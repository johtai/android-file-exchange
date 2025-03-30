package com.example.myapplication


import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Default.decodeFromString
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json

@Serializable
data class Message(val nickname: String)

lateinit var client: HttpClient

suspend fun createClient() {
    client = HttpClient(CIO) {

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
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(loadAccessToken(), loadRefreshToken())
                }
                refreshTokens {
                    val newTokens = refreshToken()
                    newTokens?.let { BearerTokens(it.first, it.second) }
                }
                sendWithoutRequest { request ->
                    !request.url.encodedPath.contains("/register") &&
                            !request.url.encodedPath.contains("/login")
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000  //запрос
            connectTimeoutMillis = 5000  //подключение
            socketTimeoutMillis = 5000   //чтение ответа
        }

    }

    try {
        client.sse(host = "5.165.249.136", port = 2868, path = "/events") {
            while (true) {
                incoming.collect { event ->
                    //println("Event from SSE server:")
                    //println(event.data)
                    val obj = decodeFromString<Message>(event.data!!)

                    // Здесь будет проверка события. Если про нас, то создаём сокет с сервером по нужному порту
                    if (obj.nickname == "admin") {
                        sendingData.receiveData("5.165.249.136", 2869)
                        client.close()
                    }
                }
            }
        }
    } catch (e: Exception) {
        println(e.message)
    }

    client.close()
}

suspend fun hello(): HttpStatusCode {

    val url = "http://5.165.249.136:2868/hello"

    val requestBuilder = HttpRequestBuilder().apply {
        this.url(url)
        contentType(io.ktor.http.ContentType.Application.Json)
        method = HttpMethod.Get
    }

    // Выводим запрос перед отправкой
    println("DEBUG REQUEST")
    println("URL: ${requestBuilder.url.buildString()}")
    println("Method: ${requestBuilder.method}")
    println("Headers: ${requestBuilder.headers}")
    println("Body: ${requestBuilder.body}")

    val response: HttpResponse = client.request(requestBuilder)
    return response.status
}

suspend fun loginResponse(username: String, password: String): HttpStatusCode {
    val url = "http://5.165.249.136:2868/login"
    val response: HttpResponse = client.post(url) {
        contentType(io.ktor.http.ContentType.Application.Json)
        setBody(mapOf("username" to username, "password" to password))
    }

    println("Response status: ${response.status.value} - ${response.status.description}")

    if (response.status == HttpStatusCode.OK) {
        val responseBody = try {
            response.body<Map<String, String>>()
        } catch (e: Exception) {
            println("Ошибка при парсинге ответа: ${e.message}")
            emptyMap()
        }
        val accessToken = responseBody["token"]
        val refreshToken = responseBody["refreshToken"]
        if (accessToken != null && refreshToken != null) {
            TokenStorage.saveToken("accessToken", accessToken)
            TokenStorage.saveToken("refreshToken", refreshToken)
        }
    } else {
        throw Exception("Code: ${response.status.value}\n${response.status.description}")
    }
    return response.status
}


@OptIn(InternalAPI::class)
suspend fun registResponse(username: String, password: String): HttpStatusCode {

    val url = "http://5.165.249.136:2868/register"
    val requestBuilder = HttpRequestBuilder().apply {
        this.url(url)
        contentType(io.ktor.http.ContentType.Application.Json)
        setBody(mapOf("username" to username, "password" to password))
        method = HttpMethod.Post
    }

//    // Выводим запрос перед отправкой
//    println("DEBUG REQUEST")
//    println("URL: ${requestBuilder.url.buildString()}")
//    println("Method: ${requestBuilder.method}")
//    println("Headers: ${requestBuilder.headers}")
//    println("Body: ${requestBuilder.body}")

    val response: HttpResponse = client.request(requestBuilder)

    println("${response.status.value}: ${response.status.description}")
    if (response.status.value != 201)
        throw Exception("Code: ${response.status.value}\n${response.status.description}")

    return response.status
}


suspend fun refreshToken(): Pair<String, String>? {
    val response: HttpResponse = client.post("http://5.165.249.136:2868/refresh") {
        contentType(io.ktor.http.ContentType.Application.Json)
        setBody(mapOf("refreshToken" to loadRefreshToken()))
    }

    return if (response.status == HttpStatusCode.OK) {
        val body = response.body<Map<String, String>>()

        val newAccessToken = body["token"]
        val newRefreshToken = body["refreshToken"]
        println("new access token${newAccessToken}")
        println("new refresh token${newRefreshToken}")
        if (newAccessToken != null && newRefreshToken != null) {
            TokenStorage.saveToken("accessToken", newAccessToken)
            TokenStorage.saveToken("refreshToken", newRefreshToken)
            newAccessToken to newRefreshToken
        } else null
    } else {
        println("error ${response.status.description} code ${response.status.value}")
        null
    }
}