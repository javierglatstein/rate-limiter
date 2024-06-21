package com.example.proxy

import com.example.domain.ScheduledRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.InstantSource

@Singleton
class ProxyService(
    @Inject private val client: ServiceClient,
    @Inject private val limiter: RateLimiter,
    @Inject private val publisher: ResponsePublisher,
    @Inject private val clock: InstantSource,
    @Inject private val telemetry: TelemetryService,
) {
    fun forward(req: ScheduledRequest): Boolean {
        if (!limiter.canExecute()) {
            return false
        }

        client.execute(req)
            .also {
                publisher.publish(it)
            }
        telemetry.recordRequestExecutionDuration(req, clock.instant())

        return true
    }
}
