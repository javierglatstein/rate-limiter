package com.example.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/health")
class HealthController(
    @Property(name = "aws.dynamo.endpoint") val dynamoEndpoint: String,
    @Property(name = "aws.sqs.endpoint") val sqsEndpoint: String,
) {
    @Get("/alive")
    fun alive(): HttpResponse<String> {
        val allEnvs = System.getenv()
        val strBuilder = StringBuilder()
        allEnvs.forEach { (k, v) -> strBuilder.append("$k => $v\n") }
        return HttpResponse.ok(strBuilder.toString())
    }

    @Get("/properties")
    fun props(): HttpResponse<String> {
        return HttpResponse.ok("Dynamo $dynamoEndpoint - sqsEndpoint: $sqsEndpoint")
    }
}
