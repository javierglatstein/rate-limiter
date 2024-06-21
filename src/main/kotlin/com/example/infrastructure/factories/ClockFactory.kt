package com.example.infrastructure.factories

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import java.time.InstantSource

@Factory
class ClockFactory {
    @Bean
    fun systemClock(): InstantSource {
        return InstantSource.system()
    }
}
