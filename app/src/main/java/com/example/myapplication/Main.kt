package com.example.myapplication

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.*

fun main() {
    val client = HttpClient {
        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }
    }
    runBlocking {
        client.sse(host = "0.0.0.0", port = 1234, path = "/events") {
            while (true) {
                incoming.collect { event ->
                    println("Событие от сервера:")
                    println(event) // Здесь будет проверка события. Если про нас, то создаём сокет с сервером по нужному порту

                    //
                }
            }
        }
    }
    client.close()
}