package com.example.infrastructure.factories

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Factory
class SQSClientFactory {
    @Replaces(SqsClient::class)
    @Bean
    @Requires(property = "aws.sqs.endpoint")
    @Inject
    fun sqsClient(
        @Property(name = "aws.sqs.endpoint") endpoint: String,
        @Property(name = "aws.region") region: String,
        @Property(name = "aws.accessKeyId") accessKey: String,
        @Property(name = "aws.secretKey") secretKey: String,
    ): SqsClient {
        return SqsClient.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .build()
    }
}
