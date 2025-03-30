package com.example.myapplication

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType


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

    fun deleteToken(key:String){
        val prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, "").apply()
    }
}


fun loadAccessToken(): String {
    return TokenStorage.getToken("accessToken") ?: ""
}

fun loadRefreshToken(): String {
    return TokenStorage.getToken("refreshToken") ?: ""
}

