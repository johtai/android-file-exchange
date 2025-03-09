package com.example

import com.example.model.PostgresUserRepository
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)

}

fun Application.module() {
   val repository = PostgresUserRepository()
    configureDatabases()
    configureAuth()
    configureSerialization()
    configureHTTP()
    configureRouting(repository)

}
