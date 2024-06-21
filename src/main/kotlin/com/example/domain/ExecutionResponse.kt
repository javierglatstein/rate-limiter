package com.example.domain

data class ExecutionResponse(
    val id: String,
    val requestId: String,
    val body: String?,
    val status: Int,
)
