package com.example.proxy

interface RateLimiter {
    fun canExecute(): Boolean
}
