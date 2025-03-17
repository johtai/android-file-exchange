package com.example

import com.example.model.PostgresUserRepository
import io.ktor.server.application.*

suspend fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
    createsocket()
}

fun Application.module() {
    configureSerialization()
    val repository = PostgresUserRepository()

    configureDatabases()
    configureAuth()
    configureHTTP()
    configureRouting(repository)

}
