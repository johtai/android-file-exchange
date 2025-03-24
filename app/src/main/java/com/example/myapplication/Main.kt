package com.example.myapplication


import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Default.decodeFromString
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json

@Serializable
data class Message(val nickname: String)

lateinit var client: HttpClient

suspend fun createClient() {
    client = HttpClient (CIO){

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
        install(Auth){
            bearer {
                loadTokens {
                    BearerTokens(loadAccessToken(), loadRefreshToken())
                }
                refreshTokens {
                    val newTokens = refreshToken()
                    newTokens?.let {BearerTokens(it.first, it.second)}
                }
            }
        }
    }

    client.sse(host = "5.165.249.136", port = 2868, path = "/events") {
        while (true)
        {
            incoming.collect { event ->
                //println("Event from SSE server:")
                //println(event.data)
                val obj = decodeFromString<Message>(event.data!!)

                // Здесь будет проверка события. Если про нас, то создаём сокет с сервером по нужному порту
                if (obj.nickname == "admin")
                {
                    sendingData.receiveData("5.165.249.136", 2869)
                    client.close()
                }
            }
        }
    }

    client.close()
}


@OptIn(InternalAPI::class)
suspend fun loginResponse(username:String, password: String): Boolean {
    val response: HttpResponse = client.post {
        url("http://5.165.249.136:2868/login")
        contentType(io.ktor.http.ContentType.Application.Json)
        body = setBody(mapOf("username" to username, "password" to password))
    }

    return if (response.status == HttpStatusCode.OK){
        val responseBody = response.body<Map<String, String>>()
        val accessToken = responseBody["accessToken"]
        val refreshToken = responseBody["refreshToken"]

        if (accessToken != null && refreshToken != null) {
            TokenStorage.saveToken("accessToken", accessToken)
            TokenStorage.saveToken("refreshToken", refreshToken)
            true
        } else {
            false
        }
    }
    else
        false
}


@OptIn(InternalAPI::class)
suspend fun registResponse(username:String, password: String): Boolean {
    val response: HttpResponse = client.post {
        url("http://5.165.249.136:2868/regist")
        contentType(io.ktor.http.ContentType.Application.Json)
        body = setBody(mapOf("username" to username, "password" to password))
    }
    return response.status == HttpStatusCode.Created
}