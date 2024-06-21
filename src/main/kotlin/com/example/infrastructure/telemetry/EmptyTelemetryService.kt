package com.example.infrastructure.telemetry

import com.example.proxy.TelemetryService
import jakarta.inject.Singleton
import java.time.Instant

@Singleton
class EmptyTelemetryService : TelemetryService {
    override fun recordRequestExecutionDuration(
        req: com.example.domain.ScheduledRequest,
        executedAt: Instant,
    ) {
        // TODO: Telemetry concrete implementation should be completed here using the company's telemetry tool
    }
}
