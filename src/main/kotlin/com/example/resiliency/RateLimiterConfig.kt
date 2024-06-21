package com.example.resiliency

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount

class RateLimiterConfig(
    val bucketName: String,
    val shards: Int,
    val maxRequests: Int,
    val frequencyAmount: Long,
    val frequencyUnit: ChronoUnit,
) {
    val frequency: TemporalAmount = Duration.of(frequencyAmount, frequencyUnit)

    companion object {
        const val PREFIX = "rate-limiter"
    }
}
