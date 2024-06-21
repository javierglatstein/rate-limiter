package com.example.controllers

import com.example.domain.ScheduledRequest

interface RequestPublisher {
    fun publish(request: ScheduledRequest)
}
