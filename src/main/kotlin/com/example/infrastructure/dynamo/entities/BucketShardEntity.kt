package com.example.infrastructure.dynamo.entities

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import java.time.Instant

@DynamoDbBean
class BucketShardEntity(
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute(ATT_PK)
    var bucketName: String? = null,
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute(ATT_SK)
    var shardNumber: Int? = null,
    @get:DynamoDbAttribute(ATT_TOKENS)
    var tokens: Int? = null,
    @get:DynamoDbAttribute(ATT_REFILLED_AT)
    var refilledAt: Instant? = null,
) {
    companion object {
        const val ATT_PK = "PK"
        const val ATT_SK = "SK"
        const val ATT_TOKENS = "tokens"
        const val ATT_REFILLED_AT = "refilled_at"
    }
}
