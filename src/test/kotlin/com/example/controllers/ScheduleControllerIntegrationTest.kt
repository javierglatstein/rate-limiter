package com.example.controllers

import com.example.config.BaseIntegrationTest
import com.example.infrastructure.sqs.SqsClientExtensions.getQueueUrl
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest.DELETE
import io.micronaut.http.HttpRequest.PATCH
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpRequest.PUT
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient

class ScheduleControllerIntegrationTest : BaseIntegrationTest() {
    @Property(name = "queues.scheduled-messages.name")
    private lateinit var queueName: String

    @Inject
    private lateinit var mapper: ObjectMapper

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    @Inject
    private lateinit var sqsClient: SqsClient

    private val queueUrl by lazy {
        sqsClient.getQueueUrl(queueName)
    }

    @Test
    @DisplayName(
        "receiving a GET request with no body or headers and query params " +
            "then http method, path and params should be in the scheduled request",
    )
    fun getRequestWithoutBodyOrHeaders() {
        val response =
            httpClient.toBlocking().retrieve("/some-long/path?filter=age").let {
                mapper.readValue(it, com.example.controllers.responses.SubmittingResponse::class.java)
            }

        val scheduledRequests = fetchScheduledRequests()

        Assertions.assertEquals(1, scheduledRequests.size)

        val scheduledRequest = scheduledRequests.first()

        Assertions.assertEquals(response.id, scheduledRequest.id)
        Assertions.assertEquals(com.example.domain.HttpMethod.GET, scheduledRequest.method)
        Assertions.assertNull(scheduledRequest.body)
        Assertions.assertEquals("/some-long/path", scheduledRequest.path)
        Assertions.assertEquals(1, scheduledRequest.params.size)
        Assertions.assertEquals(listOf("age"), scheduledRequest.params["filter"])
        val extraHeaders = scheduledRequest.headers.toMutableMap()
        extraHeaders.remove("host")
        extraHeaders.remove("connection")
        Assertions.assertEquals(emptyMap<String, List<String>>(), extraHeaders)
    }

    @Test
    @DisplayName(
        "receiving a POST request with a body and extra headers then http method, path, body and headers should schedule a new request",
    )
    fun postRequestWithBodyAndHeaders() {
        val response =
            httpClient.toBlocking()
                .exchange(
                    POST("/shortpath", """{"some":"value"}""")
                        .header("x-custom-header", "some-header-value")
                        .contentType(MediaType.APPLICATION_JSON_TYPE),
                    String::class.java,
                ).let {
                    mapper.readValue(it.body(), com.example.controllers.responses.SubmittingResponse::class.java)
                }

        val scheduledRequests = fetchScheduledRequests()

        Assertions.assertEquals(1, scheduledRequests.size)

        val scheduledRequest = scheduledRequests.first()

        Assertions.assertEquals(response.id, scheduledRequest.id)
        Assertions.assertEquals(com.example.domain.HttpMethod.POST, scheduledRequest.method)
        Assertions.assertEquals("""{"some":"value"}""", scheduledRequest.body)
        Assertions.assertEquals("/shortpath", scheduledRequest.path)
        Assertions.assertEquals(emptyMap<String, List<String>>(), scheduledRequest.params)
        Assertions.assertEquals(listOf("some-header-value"), scheduledRequest.headers["x-custom-header"])
        Assertions.assertEquals(listOf("application/json"), scheduledRequest.headers["Content-Type"])
    }

    @Test
    @DisplayName("receiving a PUT request with a body then http method, body and path should be in the new scheduled request")
    fun putRequestWithBodyAndHeaders() {
        val response =
            httpClient.toBlocking()
                .exchange(
                    PUT("/resources/123", """{"key":"val"}""")
                        .contentType(MediaType.APPLICATION_JSON_TYPE),
                    String::class.java,
                ).let {
                    mapper.readValue(it.body(), com.example.controllers.responses.SubmittingResponse::class.java)
                }

        val scheduledRequests = fetchScheduledRequests()

        Assertions.assertEquals(1, scheduledRequests.size)

        val scheduledRequest = scheduledRequests.first()

        Assertions.assertEquals(response.id, scheduledRequest.id)
        Assertions.assertEquals(com.example.domain.HttpMethod.PUT, scheduledRequest.method)
        Assertions.assertEquals("""{"key":"val"}""", scheduledRequest.body)
        Assertions.assertEquals("/resources/123", scheduledRequest.path)
        Assertions.assertEquals(emptyMap<String, List<String>>(), scheduledRequest.params)
        Assertions.assertEquals(listOf("application/json"), scheduledRequest.headers["Content-Type"])
    }

    @Test
    @DisplayName("receiving a PATCH request with a body then http method, body and path should be in the new scheduled request")
    fun patchRequestWithBodyAndHeaders() {
        val response =
            httpClient.toBlocking()
                .exchange(
                    PATCH("/resources/123", """{"new_field":"new_value"}""")
                        .contentType(MediaType.APPLICATION_JSON_TYPE),
                    String::class.java,
                ).let {
                    mapper.readValue(it.body(), com.example.controllers.responses.SubmittingResponse::class.java)
                }

        val scheduledRequests = fetchScheduledRequests()

        Assertions.assertEquals(1, scheduledRequests.size)

        val scheduledRequest = scheduledRequests.first()

        Assertions.assertEquals(response.id, scheduledRequest.id)
        Assertions.assertEquals(com.example.domain.HttpMethod.PATCH, scheduledRequest.method)
        Assertions.assertEquals("""{"new_field":"new_value"}""", scheduledRequest.body)
        Assertions.assertEquals("/resources/123", scheduledRequest.path)
        Assertions.assertEquals(emptyMap<String, List<String>>(), scheduledRequest.params)
        Assertions.assertEquals(listOf("application/json"), scheduledRequest.headers["Content-Type"])
    }

    @Test
    @DisplayName("receiving a DELETE request without a body then http method and path should be in the new scheduled request")
    fun deleteRequestWithBodyAndHeaders() {
        val response =
            httpClient.toBlocking()
                .exchange(
                    DELETE("/resources/123", null),
                    String::class.java,
                ).let {
                    mapper.readValue(it.body(), com.example.controllers.responses.SubmittingResponse::class.java)
                }

        val scheduledRequests = fetchScheduledRequests()

        Assertions.assertEquals(1, scheduledRequests.size)

        val scheduledRequest = scheduledRequests.first()

        Assertions.assertEquals(response.id, scheduledRequest.id)
        Assertions.assertEquals(com.example.domain.HttpMethod.DELETE, scheduledRequest.method)
        Assertions.assertNull(scheduledRequest.body)
        Assertions.assertEquals("/resources/123", scheduledRequest.path)
        Assertions.assertEquals(emptyMap<String, List<String>>(), scheduledRequest.params)
    }

    private fun fetchScheduledRequests(): List<com.example.domain.ScheduledRequest> {
        val sqsResponse =
            sqsClient.receiveMessage { builder ->
                builder.queueUrl(queueUrl)
                    .waitTimeSeconds(30)
            }

        return sqsResponse.messages()
            .map {
                mapper.readValue(it.body(), com.example.domain.ScheduledRequest::class.java)
            }
    }
}
