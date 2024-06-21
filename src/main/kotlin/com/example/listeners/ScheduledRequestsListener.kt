package com.example.listeners

import com.example.domain.ScheduledRequest
import com.example.proxy.ProxyService
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Requires
import io.micronaut.jms.annotations.JMSListener
import io.micronaut.jms.annotations.Message
import io.micronaut.jms.annotations.Queue
import io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME
import io.micronaut.messaging.annotation.MessageBody
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.jms.Session

@Requires(property = "micronaut.jms.sqs.enabled", value = "true")
@JMSListener(CONNECTION_FACTORY_BEAN_NAME)
class ScheduledRequestsListener(
    @Inject private val proxyService: ProxyService,
    @Inject private val mapper: ObjectMapper,
) {
    companion object {
        private val logger: Logger =
            LoggerFactory.getLogger(ScheduledRequestsListener::class.java)
    }

    @Queue(
        value = "\${queues.scheduled-messages.name}",
        acknowledgeMode = Session.CLIENT_ACKNOWLEDGE,
    )
    fun receive(
        @Message msg: javax.jms.Message,
        @MessageBody body: String,
    ) {
        logger.info("received message $body")
        val req = mapper.readValue(body, ScheduledRequest::class.java)
        if (proxyService.forward(req)) {
            msg.acknowledge()
        }
    }
}
