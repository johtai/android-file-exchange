package com.example.myapplication

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Default.decodeFromString
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Serializable
data class Message(val nickname: String)

fun createClient() {
    val client = HttpClient (CIO){
        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

//    runBlocking {
//        client.sse(host = "5.167.121.51", port = 2868, path = "/events") {
//            while (true) {
//                incoming.collect { event ->
//                    println("Event from the server:")
//                    //println(event.data)
//                    val obj = decodeFromString<Message>(event.data!!)
//
//                    // Здесь будет проверка события. Если про нас, то создаём сокет с сервером по нужному порту
//                    if (obj.nickname == "admin")
//                    {
//                        println("It's works!")
//                    }
//                }
//            }
//        }
//    }

    //client.close()
}