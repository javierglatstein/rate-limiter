package com.example.resiliency

import com.example.proxy.RateLimiter
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class TokenBucketRateLimiter(
    @Inject private val config: RateLimiterConfig,
    @Inject private val shardRepository: BucketShardRepository,
) : RateLimiter {
    override fun canExecute(): Boolean {
        var canExecute = shardRepository.consumeToken(config.bucketName, randomShardNumber())

        if (!canExecute) {
            shardRepository.refill(
                config.bucketName,
                randomShardNumber(),
                config.maxRequests,
                config.frequency,
            )
            canExecute = shardRepository.consumeToken(config.bucketName, randomShardNumber())
        }

        return canExecute
    }

    private fun randomShardNumber(): Int = (Math.random() * config.shards + 1).toInt()
}
