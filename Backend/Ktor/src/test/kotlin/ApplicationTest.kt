package com.example

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

//    @Test
//    fun testRoot() = testApplication {
//        // Создаем конфигурацию вручную
//        val config = MapApplicationConfig().apply {
//            put("ktor.deployment.port", "2868")
//            put("ktor.deployment.host", "0.0.0.0")
//            put("jwt.secret", "your_secret_key_here")
//            put("jwt.issuer", "http://0.0.0.0:2868")
//            put("jwt.audience", "http://0.0.0.0:2868/hello")
//            put("jwt.realm", "Access to 'hello'")
//        }
//
//        environment {
//            this.config = config
//        }
//
//        application {
//            module()
//        }
//        client.get("/").apply {
//            assertEquals(HttpStatusCode.OK, status)
//        }
//    }

}
