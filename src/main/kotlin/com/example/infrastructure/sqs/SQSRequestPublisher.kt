package com.example.infrastructure.sqs

import com.example.controllers.RequestPublisher
import com.example.domain.ScheduledRequest
import com.example.infrastructure.sqs.SqsClientExtensions.getQueueUrl
import com.example.infrastructure.sqs.SqsClientExtensions.sendMessage
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import software.amazon.awssdk.services.sqs.SqsClient

@Singleton
class SQSRequestPublisher(
    @Property(name = "queues.scheduled-messages.name") private val queueName: String,
    @Inject private val client: SqsClient,
    @Inject private val objectMapper: ObjectMapper,
) : RequestPublisher {
    private val url by lazy {
        client.getQueueUrl(queueName)
    }

    override fun publish(request: ScheduledRequest) {
        client.sendMessage(url, objectMapper.writeValueAsString(request))
    }
}
