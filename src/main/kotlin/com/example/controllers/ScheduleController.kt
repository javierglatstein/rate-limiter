package com.example.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import jakarta.inject.Inject
import java.time.InstantSource
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Controller("/")
class ScheduleController(
    @Inject private val publisher: RequestPublisher,
    @Inject private val clock: InstantSource,
    @Inject private val mapper: ObjectMapper,
) {
    @Get("{/path:.*}")
    fun getForward(
        request: HttpRequest<*>,
        @Suppress("UNUSED_PARAMETER") path: String?,
    ): HttpResponse<*> {
        return forward(request)
    }

    @Post("{/path:.*}")
    fun postForward(
        request: HttpRequest<*>,
        @Suppress("UNUSED_PARAMETER") path: String?,
        @Suppress("UNUSED_PARAMETER") @Body body: String?,
    ): HttpResponse<*> {
        return forward(request)
    }

    @Put("{/path:.*}")
    fun putForward(
        request: HttpRequest<*>,
        @Suppress("UNUSED_PARAMETER") path: String?,
        @Suppress("UNUSED_PARAMETER") @Body body: String?,
    ): HttpResponse<*> {
        return forward(request)
    }

    @Patch("{/path:.*}")
    fun patchForward(
        request: HttpRequest<*>,
        @Suppress("UNUSED_PARAMETER") path: String?,
        @Suppress("UNUSED_PARAMETER") @Body body: String?,
    ): HttpResponse<*> {
        return forward(request)
    }

    @Delete("{/path:.*}")
    fun deleteForward(
        request: HttpRequest<*>,
        @Suppress("UNUSED_PARAMETER") path: String?,
    ): HttpResponse<*> {
        return forward(request)
    }

    private fun forward(request: HttpRequest<*>): HttpResponse<*> {
        val req =
            com.example.domain.ScheduledRequest(
                id = UUID.randomUUID().toString(),
                createdAt = clock.instant(),
                method = parseFromFramework(request.method),
                path = request.path,
                body = request.body.getOrNull()?.toString(),
                headers = request.headers.asMap(),
                params = request.parameters.asMap(),
            )

        publisher.publish(req)

        return HttpResponse.created(mapper.writeValueAsString(com.example.controllers.responses.SubmittingResponse(id = req.id)))
    }

    private fun parseFromFramework(method: io.micronaut.http.HttpMethod): com.example.domain.HttpMethod {
        return com.example.domain.HttpMethod.valueOf(method.toString())
    }
}
