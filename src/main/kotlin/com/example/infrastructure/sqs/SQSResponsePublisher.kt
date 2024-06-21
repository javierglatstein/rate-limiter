package com.example.infrastructure.sqs

import com.example.infrastructure.sqs.SqsClientExtensions.getQueueUrl
import com.example.infrastructure.sqs.SqsClientExtensions.sendMessage
import com.example.proxy.ResponsePublisher
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import software.amazon.awssdk.services.sqs.SqsClient

@Singleton
class SQSResponsePublisher(
    @Property(name = "queues.processed-messages.name") private val queueName: String,
    @Inject private val client: SqsClient,
    @Inject private val objectMapper: ObjectMapper,
) : ResponsePublisher {
    private val url by lazy {
        client.getQueueUrl(queueName)
    }

    override fun publish(response: com.example.domain.ExecutionResponse) {
        client.sendMessage(url, objectMapper.writeValueAsString(response))
    }
}
