package com.example.resiliency

import java.time.temporal.TemporalAmount

interface BucketShardRepository {
    fun consumeToken(
        bucketName: String,
        shard: Int,
    ): Boolean

    fun refill(
        bucketName: String,
        shard: Int,
        tokens: Int,
        refillPeriod: TemporalAmount,
    )
}
