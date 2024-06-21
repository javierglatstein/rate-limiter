package com.example.proxy

import com.example.domain.ScheduledRequest
import java.time.Instant

interface TelemetryService {
    fun recordRequestExecutionDuration(
        req: ScheduledRequest,
        executedAt: Instant,
    )
}
