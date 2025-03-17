package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        //swaggerUI(path = "openapi", swaggerFile = "D:\\programming2025\\ktor\\ktor3\\src\\main\\resources\\openapi\\documentation.yaml")
        swaggerUI(path = "openapi", swaggerFile = "openapi/documentation.yaml")

    }
    install(CORS) {

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }





}
