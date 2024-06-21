package com.example.proxy

import com.example.domain.ExecutionResponse
import com.example.domain.ScheduledRequest

interface ServiceClient {
    fun execute(req: ScheduledRequest): ExecutionResponse
}
