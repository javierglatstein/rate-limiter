package com.example.infrastructure.dynamo

import com.example.infrastructure.dynamo.entities.BucketShardEntity
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@Singleton
class DynamoShardsClient(
    @Property(name = "rate-limiter.table-name") private val tableName: String,
    @Inject private val client: DynamoDbClient,
) {
    private val table by lazy {
        val enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(client).build()
        val schema = TableSchema.fromBean(BucketShardEntity::class.java)
        enhancedClient.table(tableName, schema)
    }

    fun updateShard(shard: BucketShardEntity) {
        table.updateItem(shard)
    }

    fun getShard(
        bucketName: String,
        shardNumber: Int,
    ): BucketShardEntity {
        val key = Key.builder().partitionValue(bucketName).sortValue(shardNumber).build()
        return table.getItem(key)
    }
}
