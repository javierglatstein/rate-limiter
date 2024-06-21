package com.example.infrastructure.factories

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Factory
class OkHttpClientFactory {
    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .build()
    }
}
