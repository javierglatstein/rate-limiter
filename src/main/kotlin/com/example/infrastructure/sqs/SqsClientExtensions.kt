package com.example.infrastructure.sqs

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

object SqsClientExtensions {
    fun SqsClient.getQueueUrl(name: String): String =
        this.getQueueUrl(
            GetQueueUrlRequest.builder()
                .queueName(name)
                .build(),
        ).queueUrl()

    fun SqsClient.sendMessage(
        url: String,
        message: String,
    ) {
        this.sendMessage(
            SendMessageRequest.builder()
                .queueUrl(url)
                .messageBody(message)
                .build(),
        )
    }
}
