package com.example.myapplication

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

suspend fun main() {
    val client = HttpClient(CIO)

    client.close()
}