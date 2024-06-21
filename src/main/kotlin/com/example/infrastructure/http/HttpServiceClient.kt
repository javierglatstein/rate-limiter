package com.example.infrastructure.http

import com.example.proxy.ServiceClient
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

@Singleton
class HttpServiceClient(
    @Property(name = "proxy.host.scheme") private val scheme: String,
    @Property(name = "proxy.host.name") private val host: String,
    @Property(name = "proxy.host.port") private val port: Int,
    @Inject private val client: OkHttpClient,
) : ServiceClient {
    override fun execute(req: com.example.domain.ScheduledRequest): com.example.domain.ExecutionResponse {
        var sanitizedPath = req.path
        if (sanitizedPath.startsWith("/")) {
            sanitizedPath = sanitizedPath.substring(1)
        }

        val urlBuilder =
            HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .addPathSegments(sanitizedPath)
        req.params.forEach { (key, values) ->
            values.forEach { value ->
                urlBuilder.addQueryParameter(key, value)
            }
        }

        val reqBuilder =
            Request.Builder()
                .url(urlBuilder.build())
                .method(req.method.toString(), req.body?.toRequestBody())
        req.headers.forEach { (key, values) ->
            values.forEach { value ->
                reqBuilder.addHeader(key, value)
            }
        }

        val httpResponse = client.newCall(reqBuilder.build()).execute()
        return com.example.domain.ExecutionResponse(
            id = UUID.randomUUID().toString(),
            requestId = req.id,
            body = httpResponse.body?.string(),
            status = httpResponse.code,
        )
    }
}
