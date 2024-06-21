package com.example.domain

import java.time.Instant

data class ScheduledRequest(
    val id: String,
    val createdAt: Instant,
    val method: HttpMethod,
    val path: String,
    val body: String?,
    val headers: Map<String, List<String>>,
    val params: Map<String, List<String>>,
)
