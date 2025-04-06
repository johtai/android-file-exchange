package com.example

import com.example.model.PostgresUserRepository
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel

val messageChannel = Channel<String>(capacity = Channel.UNLIMITED)

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    val repository = PostgresUserRepository()
    configureDatabases()
    configureAuth()
    configureHTTP()
    configureRouting(repository)
    createSocket()
}
