package com.example.infrastructure.dynamo

import com.example.infrastructure.dynamo.entities.BucketShardEntity.Companion.ATT_PK
import com.example.infrastructure.dynamo.entities.BucketShardEntity.Companion.ATT_REFILLED_AT
import com.example.infrastructure.dynamo.entities.BucketShardEntity.Companion.ATT_SK
import com.example.infrastructure.dynamo.entities.BucketShardEntity.Companion.ATT_TOKENS
import com.example.resiliency.BucketShardRepository
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import java.time.InstantSource
import java.time.temporal.TemporalAmount

@Singleton
class DynamoBucketShardRepository(
    @Property(name = "rate-limiter.table-name") private val tableName: String,
    @Inject private val client: DynamoDbClient,
    @Inject private val clock: InstantSource,
) : BucketShardRepository {
    companion object {
        private val logger: Logger =
            LoggerFactory.getLogger(DynamoBucketShardRepository::class.java)
    }

    override fun consumeToken(
        bucketName: String,
        shard: Int,
    ): Boolean {
        try {
            client.updateItem { request ->
                request.tableName(tableName)
                    .key(
                        mapOf(
                            ATT_PK to AttributeValues.stringValue(bucketName),
                            ATT_SK to AttributeValues.numberValue(shard),
                        ),
                    )
                    .updateExpression("SET #tokens = #tokens - :amount")
                    .conditionExpression("#tokens >= :amount")
                    .expressionAttributeNames(
                        mapOf("#tokens" to ATT_TOKENS),
                    )
                    .expressionAttributeValues(
                        mapOf(":amount" to AttributeValues.numberValue(1)),
                    )
            }
        } catch (e: ConditionalCheckFailedException) {
            logger.debug("no tokens available on shard $shard")
            return false
        }
        return true
    }

    override fun refill(
        bucketName: String,
        shard: Int,
        tokens: Int,
        refillPeriod: TemporalAmount,
    ) {
        try {
            val now = clock.instant()
            val minimumToRefill = now.minus(refillPeriod)

            client.updateItem { request ->
                request.tableName(tableName)
                    .key(
                        mapOf(
                            ATT_PK to AttributeValues.stringValue(bucketName),
                            ATT_SK to AttributeValues.numberValue(shard),
                        ),
                    )
                    .updateExpression("SET #tokens = :amount, #refilled_at = :now")
                    .conditionExpression("#refilled_at <= :minimum_to_refill")
                    .expressionAttributeNames(
                        mapOf(
                            "#tokens" to ATT_TOKENS,
                            "#refilled_at" to ATT_REFILLED_AT,
                        ),
                    )
                    .expressionAttributeValues(
                        mapOf(
                            ":amount" to AttributeValues.numberValue(tokens),
                            ":minimum_to_refill" to AttributeValues.stringValue(minimumToRefill.toString()),
                            ":now" to AttributeValues.stringValue(now.toString()),
                        ),
                    )
            }
        } catch (_: ConditionalCheckFailedException) {
            logger.debug("too soon to refill bucket's shard")
        }
    }
}
