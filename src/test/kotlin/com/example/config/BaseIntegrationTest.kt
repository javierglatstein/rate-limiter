package com.example.config

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import io.micronaut.context.annotation.ContextConfigurer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@ContextConfigurer
@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest : TestPropertyProvider {
    companion object {
        private const val WEB_SERVER_PORT = 3055
        private const val LOCALSTACK_SCRIPTS_HOST_PATH = "./src/test/resources/localstack/scripts"
        private val localStackContainer: LocalStackContainer =
            LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5"))
                .withServices(LocalStackContainer.Service.DYNAMODB)
                .withServices(LocalStackContainer.Service.SQS)
                .withFileSystemBind(LOCALSTACK_SCRIPTS_HOST_PATH, "/etc/localstack/init/ready.d", BindMode.READ_ONLY)
                .waitingFor(
                    Wait.forLogMessage(".*arn:aws:dynamodb:.*/local-table.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(1)),
                )

        init {
            localStackContainer.start()
        }

        @JvmStatic
        @RegisterExtension
        protected val mockWebServer: WireMockExtension =
            WireMockExtension.newInstance()
                .options(WireMockConfiguration.wireMockConfig().port(WEB_SERVER_PORT))
                .build()
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.resetAll()
    }

    protected fun mockJsonCall(
        method: String,
        url: String,
        responseBody: String,
        responseCode: Int,
    ) {
        val builder =
            when (method) {
                "GET" -> WireMock.get(urlEqualTo(url))
                "POST" -> WireMock.post(urlEqualTo(url))
                "PUT" -> WireMock.put(urlEqualTo(url))
                "PATCH" -> WireMock.patch(urlEqualTo(url))
                "DELETE" -> WireMock.delete(urlEqualTo(url))
                else -> throw RuntimeException("invalid mock method $method")
            }
        mockWebServer.stubFor(
            builder.willReturn(
                ResponseDefinitionBuilder.responseDefinition()
                    .withBody(responseBody)
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withStatus(responseCode),
            ),
        )
    }

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "aws.services.sqs.endpoint-override" to
                localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS)
                    .toString(),
            "aws.sqs.endpoint" to
                localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS)
                    .toString(),
            "aws.services.dynamo.endpoint-override" to
                localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB)
                    .toString(),
            "aws.dynamo.endpoint" to
                localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB)
                    .toString(),
            "proxy.host.port" to "$WEB_SERVER_PORT",
        )
}
