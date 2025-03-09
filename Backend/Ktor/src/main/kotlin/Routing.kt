package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.db.UserDAO
import com.example.db.UserTable
import com.example.db.suspendTransaction
import com.example.model.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

fun Application.configureRouting(repository: UserRepository)
{

    var secret = environment.config.property("jwt.secret").getString()
    var issuer = environment.config.property("jwt.issuer").getString()
    var audience = environment.config.property("jwt.audience").getString()

    routing {

        post("/login"){
            val user = call.receive<User>()
            // Check username and password
            val userDAO = suspendTransaction {   UserDAO.find { UserTable.username eq user.username}.firstOrNull()}
            if (userDAO != null && userDAO.password == user.password){
                val token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("username", user.username)
                    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                    .sign(Algorithm.HMAC256(secret))
                call.respond(hashMapOf("token" to token))
            } else{
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        authenticate("auth-jwt")
        {
            get("/hello"){
                //val userSession = call.principal<UserSession>()
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
                //call.respondText("Сессия прошла!")
            }

        }

        get("/"){
            call.respondText("Hello, ${call.principal<CustomPrincipal>()?.userName}!")
        }

        get("/file") {
            val filepath = "./src/main/resources/abc.txt"
            val file = File(filepath)

            if(file != null)
            {
                try
                {
                    val fileContent = file.readText()
                    call.respondText(fileContent)
                }catch (ex: Exception){
                    call.respondText(ex.toString())
                }
            }else{
                call.respondText("Файл не найден", status = HttpStatusCode.NotFound)
            }
        }

        get("/status"){
            call.respondText("Сервер запущен!", status = HttpStatusCode.OK)
        }

//        route("/tasks"){
//            get{
//                val tasks = repository.allTasks()
//                call.respond(tasks)
//            }
//
//            get("/byName/{taskName}"){
//                val name = call.parameters["taskName"]
//                if(name == null){
//                    call.respond(HttpStatusCode.BadRequest)
//                    return@get
//                }
//                val task = repository.taskByName(name)
//                if(task == null){
//                    call.respond(HttpStatusCode.NotFound)
//                    return@get
//                }
//                call.respond(task)
//            }
//
//            get("/byPriority/{priority}") {
//                val priorityparam = call.parameters["priority"]
//                if (priorityparam == null) {
//                    call.respond(HttpStatusCode.BadRequest)
//                    return@get
//                }
//                try {
//                    val priority = Priority.valueOf(priorityparam)
//                    val tasks = repository.tasksByPriority(priority)
//
//
//                    if (tasks.isEmpty()) {
//                        call.respond(HttpStatusCode.NotFound)
//                        return@get
//                    }
//                    call.respond(tasks)
//                } catch (ex: IllegalArgumentException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                }
//            }
//
//            post {
//                try {
//                    val task = call.receive<Task>()
//                    repository.addTask(task)
//                    call.respond(HttpStatusCode.NoContent)
//                    return@post
//                } catch (ex: IllegalStateException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                } catch (ex: JsonConvertException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                }
//                call.respondText("Already exist")
//            }
//
//        }

        get("/files"){
            val directoryPath = "./src/main/resources/"
            val directory = File(directoryPath)

            if(directory.exists() && directory.isDirectory)
            {
                val fileNames = directory.listFiles().map{it.name}
                call.respondText(fileNames.joinToString(separator = "\n"))
            }else{
                call.respondText("Дирректория не найдена", status = HttpStatusCode.NotFound)
        }
        }

        get("/file/{filename}"){
            val filename = call.parameters["filename"]
            val filepath = "./src/main/resources/$filename"
            val file = File(filepath)

            if(file.exists()){
                call.respondText("Файл $filename существует на сервере")
            }else{
                call.respondText("Файл $filename не найден на сервере", status = HttpStatusCode.NotFound)

            }
        }

    }
}
