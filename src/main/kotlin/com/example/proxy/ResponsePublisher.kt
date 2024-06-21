package com.example.proxy

import com.example.domain.ExecutionResponse

interface ResponsePublisher {
    fun publish(response: ExecutionResponse)
}
