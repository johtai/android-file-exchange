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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Serializable
data class Message(val nickname: String)

lateinit var client: HttpClient

fun createClient() {
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

fun loadAccessToken(): String {
    return TokenStorage.getToken("accessToken") ?: ""
}

fun loadRefreshToken(): String {
    return TokenStorage.getToken("refreshToken") ?: ""
}

suspend fun refreshToken(): Pair<String, String>? {
    val response: HttpResponse = client.post("https://your-server.com/refresh") {
        contentType(io.ktor.http.ContentType.Application.Json)
        setBody(mapOf("refreshToken" to loadRefreshToken()))
    }

    return if (response.status == HttpStatusCode.OK) {
        val body = response.body<Map<String, String>>()
        val newAccessToken = body["accessToken"]
        val newRefreshToken = body["refreshToken"]
        if (newAccessToken != null && newRefreshToken != null) {
            TokenStorage.saveToken("accessToken", newAccessToken)
            TokenStorage.saveToken("refreshToken", newRefreshToken)
            newAccessToken to newRefreshToken
        } else null
    } else {
        null
    }
}
object TokenStorage {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getToken(key: String): String? {
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    fun saveToken(key: String, value: String) {
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }
}
