package com.example.listeners

import com.example.infrastructure.factories.ObjectMapperFactory
import com.example.proxy.ProxyService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.jms.Message

class ScheduledRequestsListenerUnitTest {
    private val jsonRequest =
        """{
        "id":"req-9876",
        "created_at":"2024-03-15T09:15:32.000Z",
        "method":"GET",
        "path":"/path/to/123",
        "body":null,
        "headers":{},
        "params":{}
    }
        """.trimMargin()

    @Test
    @DisplayName("when processing a message and forwarding it successfully then the message should be acknowledged")
    fun acknowledgeMessageAfterForwardingIt() {
        val proxy: ProxyService = mockk()
        val mapper: ObjectMapper = ObjectMapperFactory().objectMapper()

        val listener = ScheduledRequestsListener(proxy, mapper)

        val msg: Message = mockk()
        every { proxy.forward(any()) } returns true
        every { msg.acknowledge() } just runs

        listener.receive(msg, jsonRequest)
    }

    @Test
    @DisplayName("when processing a message and not being able to forward it successfully then the message should not be acknowledged")
    fun dontAcknowledgeMessageWhenCantForwardIt() {
        val proxy: ProxyService = mockk()
        val mapper: ObjectMapper = ObjectMapperFactory().objectMapper()

        val listener = ScheduledRequestsListener(proxy, mapper)

        val msg: Message = mockk()
        every { proxy.forward(any()) } returns false
        listener.receive(msg, jsonRequest)
    }
}
