package com.example.proxy

import com.example.config.BaseIntegrationTest
import com.example.infrastructure.dynamo.DynamoShardsClient
import com.example.infrastructure.dynamo.entities.BucketShardEntity
import com.example.infrastructure.sqs.SqsClientExtensions.getQueueUrl
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import java.time.Instant
import java.time.InstantSource

@Property(name = "rate-limiter.max-requests", value = "5")
@Property(name = "rate-limiter.shards", value = "1")
@Property(name = "rate-limiter.bucket-name", value = "proxy-service-int-test-bucket")
@WireMockTest
class ProxyServiceIntegrationTest : BaseIntegrationTest() {
    @Property(name = "queues.processed-messages.name")
    private lateinit var queueName: String

    @Inject
    private lateinit var sqsClient: SqsClient

    @Inject
    private lateinit var shardsClient: DynamoShardsClient

    @Inject
    private lateinit var clock: InstantSource

    @Inject
    private lateinit var telemetry: TelemetryService

    @Inject
    private lateinit var mapper: ObjectMapper

    @Inject
    private lateinit var service: ProxyService

    private val queueUrl by lazy {
        sqsClient.getQueueUrl(queueName)
    }

    private val bucketName = "proxy-service-int-test-bucket"
    private val shardNumber = 1

    @Test
    @DisplayName("processing a request with one token left should send it to the target service and publish the response")
    fun processingRequestWithOneTokenLeft() {
        val lastRefill = Instant.parse("2024-03-15T10:00:00.000Z")
        shardsClient.updateShard(
            BucketShardEntity(
                bucketName = bucketName,
                shardNumber = shardNumber,
                tokens = 1,
                refilledAt = lastRefill,
            ),
        )

        val executionTime = lastRefill.plusMillis(500)
        every { clock.instant() } returns executionTime
        mockJsonCall("GET", "/some-route?from=NOW", """{"data":{"key":123}}""", HttpStatus.OK.code)
        val request =
            com.example.domain.ScheduledRequest(
                id = "req-123",
                createdAt = Instant.parse("2024-03-15T09:59:50.000Z"),
                method = com.example.domain.HttpMethod.GET,
                path = "/some-route",
                body = null,
                headers = emptyMap(),
                params = mapOf("from" to listOf("NOW")),
            )
        every { telemetry.recordRequestExecutionDuration(request, executionTime) } just runs

        val forwarded = service.forward(request)

        Assertions.assertTrue(forwarded)

        // Then: no more tokens should be available and no refresh should have happened
        val shard = shardsClient.getShard(bucketName, shardNumber)
        Assertions.assertEquals(0, shard.tokens)
        Assertions.assertEquals(lastRefill, shard.refilledAt)

        // Then: a response should have been published
        val responses = fetchExecutionResponses(5)
        Assertions.assertEquals(1, responses.size)

        val response = responses.first()
        Assertions.assertNotEquals("", response.id)
        Assertions.assertEquals("req-123", response.requestId)
        Assertions.assertEquals(HttpStatus.OK.code, response.status)
        Assertions.assertEquals("""{"data":{"key":123}}""", response.body)
    }

    @Test
    @DisplayName("processing a request with no token left and after refill period should refill shard and process the request")
    fun processingRequestWithoutTokenLeftAndRefill() {
        val lastRefill = Instant.parse("2024-03-15T10:00:00.000Z")
        shardsClient.updateShard(
            BucketShardEntity(
                bucketName = bucketName,
                shardNumber = shardNumber,
                tokens = 0,
                refilledAt = lastRefill,
            ),
        )

        val executionTime = lastRefill.plusSeconds(1001)
        every { clock.instant() } returns executionTime
        mockJsonCall("POST", "/news", """{"data":"done"}""", HttpStatus.CREATED.code)
        val request =
            com.example.domain.ScheduledRequest(
                id = "req-356",
                createdAt = Instant.parse("2024-03-15T09:59:50.000Z"),
                method = com.example.domain.HttpMethod.POST,
                path = "/news",
                body = """{"data":"done"}""",
                headers = mapOf("x-custom-header" to listOf("custom-value")),
                params = emptyMap(),
            )
        every { telemetry.recordRequestExecutionDuration(request, executionTime) } just runs

        val forwarded = service.forward(request)

        Assertions.assertTrue(forwarded)

        // Then: shard should've been refilled and one token used
        val shard = shardsClient.getShard(bucketName, shardNumber)
        Assertions.assertEquals(4, shard.tokens)
        Assertions.assertEquals(executionTime, shard.refilledAt)

        // Then: a response should have been published
        val responses = fetchExecutionResponses(5)
        Assertions.assertEquals(1, responses.size)

        val response = responses.first()
        Assertions.assertNotEquals("", response.id)
        Assertions.assertEquals("req-356", response.requestId)
        Assertions.assertEquals(HttpStatus.CREATED.code, response.status)
        Assertions.assertEquals("""{"data":"done"}""", response.body)
    }

    @Test
    @DisplayName("processing a request with no tokens left and before refill period should not process the request")
    fun processingRequestWithoutTokensLeftAndNoRefill() {
        val lastRefill = Instant.parse("2024-03-15T10:00:00.000Z")
        shardsClient.updateShard(
            BucketShardEntity(
                bucketName = bucketName,
                shardNumber = shardNumber,
                tokens = 0,
                refilledAt = lastRefill,
            ),
        )

        every { clock.instant() } returns lastRefill.plusMillis(500)
        mockJsonCall("PUT", "/some-resources/123", """{"field":"new_val"}""", HttpStatus.OK.code)
        val forwarded =
            service.forward(
                com.example.domain.ScheduledRequest(
                    id = "req-123",
                    createdAt = Instant.parse("2024-03-15T09:59:50.000Z"),
                    method = com.example.domain.HttpMethod.PUT,
                    path = "/some-resources/123",
                    body = """{"field":"new_val"}""",
                    headers = emptyMap(),
                    params = emptyMap(),
                ),
            )
        Assertions.assertFalse(forwarded)

        // Then: shard shouldn't have changed
        val shard = shardsClient.getShard(bucketName, shardNumber)
        Assertions.assertEquals(0, shard.tokens)
        Assertions.assertEquals(lastRefill, shard.refilledAt)

        // Then: no response should have been published
        val responses = fetchExecutionResponses(1)
        Assertions.assertEquals(0, responses.size)
    }

    @MockBean(InstantSource::class)
    fun clock(): InstantSource {
        return mockk()
    }

    @MockBean(TelemetryService::class)
    fun telemetryService(): TelemetryService {
        return mockk()
    }

    private fun fetchExecutionResponses(waitInSeconds: Int): List<com.example.domain.ExecutionResponse> {
        val sqsResponse =
            sqsClient.receiveMessage { builder ->
                builder.queueUrl(queueUrl)
                    .waitTimeSeconds(waitInSeconds)
            }

        return sqsResponse.messages()
            .map {
                mapper.readValue(it.body(), com.example.domain.ExecutionResponse::class.java)
            }
    }
}
