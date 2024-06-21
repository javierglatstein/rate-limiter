package com.example.resiliency

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import java.time.temporal.ChronoUnit

@Factory
internal class RateLimiterConfigFactory {
    @Bean
    @Inject
    fun rateLimiterConfig(
        @Property(name = RateLimiterConfig.PREFIX + ".bucket-name") bucketName: String,
        @Property(name = RateLimiterConfig.PREFIX + ".shards") shards: Int,
        @Property(name = RateLimiterConfig.PREFIX + ".max-requests") maxRequests: Int,
        @Property(name = RateLimiterConfig.PREFIX + ".frequency-amount") frequencyAmount: Long,
        @Property(name = RateLimiterConfig.PREFIX + ".frequency-unit") frequencyUnit: ChronoUnit,
    ): RateLimiterConfig {
        return RateLimiterConfig(
            bucketName = bucketName,
            shards = shards,
            maxRequests = maxRequests,
            frequencyAmount = frequencyAmount,
            frequencyUnit = frequencyUnit,
        )
    }
}
