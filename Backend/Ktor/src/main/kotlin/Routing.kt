package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.db.*
import com.example.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import java.math.BigInteger
import java.security.MessageDigest

//MD5-конвертер для пароля
fun md5(input: String): String{
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken:String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

fun Application.configureRouting(repository: UserRepository)
{

    var secret = environment.config.property("jwt.secret").getString()
    var issuer = environment.config.property("jwt.issuer").getString()
    var audience = environment.config.property("jwt.audience").getString()

    routing {

        // Получение JWT-токена по имени пользователя и паролю
        post("/login"){
            println("Try to /login")
            try {
                val user = call.receive<User>()
                // Check username and password
                val userDAO = suspendTransaction { UserDAO.find { UserTable.username eq user.username }.firstOrNull() }
                if (userDAO != null && userDAO.password == md5(user.password))
                {
                    val refreshTokenRepository = PostgresRefreshTokenRepository() //отсюда берём CRUD операции с refreshToken table
                    val storedToken = refreshTokenRepository.findByName(user.username)

                    //Если токен существует, то удаляем его
                    if(storedToken != null)
                    {
                     refreshTokenRepository.removeRefreshToken(user.username)
                    }

                    val accessToken = JWT.create()
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .withClaim("username", user.username)
                        .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 минут
                        .sign(Algorithm.HMAC256(secret))

                    val refreshToken = UUID.randomUUID().toString()
                    val refreshTokenExpiresAt = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) //7 days

                    // save refresh token at BD
                    suspendTransaction {
                        RefreshTokenDAO.new {
                            username = user.username
                            token = refreshToken
                            expiresAt = refreshTokenExpiresAt.toLocalDate()
                        }
                    }

                    println("Login with create JWT was Successfully")
                    call.respond(AuthResponse(accessToken, refreshToken))
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }catch (e: Exception){
                println("Error in /login: ${e.message}")
                println(e.printStackTrace())
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        //endpoint для обновления токена
        post("/refresh"){
            println("Try to /refresh")
            val request = call.receive<RefreshRequest>()
            var refreshTokenRepository = PostgresRefreshTokenRepository()

            // Проверяем refresh token в БД
            try {

                //val storedToken = suspendTransaction { RefreshTokenDAO.find { RefreshTokenTable.token eq request.refreshToken} }.firstOrNull()
                val storedToken = refreshTokenRepository.findByToken(request.refreshToken)
                println("storedToken = $storedToken")
                if (storedToken == null) {//storedToken.expiresAt){
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }


                // Удаляем использованный Refresh token
                refreshTokenRepository.removeRefreshToken(storedToken.username)

                // Генерация нового access token
                val newAccessToken = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("username", storedToken.username)
                    .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 минут
                    .sign(Algorithm.HMAC256(secret))


                val newRefreshToken = UUID.randomUUID().toString()
                val newRefreshTokenExpiresAt = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) //7 days

                //Сохранение refresh token в БД
                suspendTransaction {
                    RefreshTokenDAO.new {
                        username = storedToken.username
                        token = newRefreshToken
                        expiresAt = newRefreshTokenExpiresAt.toLocalDate()
                    }
                }

                call.respond(AuthResponse(newAccessToken, newRefreshToken))
            } catch (e : Exception){
                println("Error in /refresh: ${e.message}")
                println(e.printStackTrace())
                call.respond(HttpStatusCode.InternalServerError)
            }
        }


        // регистрация на сайте через имя пользователя и пароль
        post("/register"){
            println("Try to /register")
            try {
                val user = call.receive<User>()
                val pswd = md5(user.password)
                suspendTransaction {
                    UserDAO.new {
                        username = user.username
                        password = pswd
                    }
                }
                println("New user was created!")
                call.respond(HttpStatusCode.Created)
            }catch (e: Exception){
                println("Error in /registed ${e.message}")
                println(e.printStackTrace())
                call.respond(HttpStatusCode.InternalServerError)
            }

//            val username = call.parameters["username"]
//            val password = call.parameters["password"]
//            if (username != null && password != null) {
//                suspendTransaction {
//                    UserDAO.new {
//                        this.username = username
//                        this.password = password
//                    }
//                }
//                call.respond(HttpStatusCode.Created)
//            } else {
//                call.respond(HttpStatusCode.BadRequest)
//            }

        }

        sse("/events") {
            call.application.environment.log.info("/events was touched")
            val message = Message("sse_connected")
            val jsonMessage = Json.encodeToString(message)
            send(ServerSentEvent(event = "jsonEvent", data = jsonMessage))
            
            for(message in messageChannel){
               //val event = ServerSentEvent(event = "jsonevent", )
               println("отправка message admin:  $message")
               send(ServerSentEvent(event = "jsonEvent", data = message))
            }

        }




        // Вход на /hello аунтентификационным пользователям
        authenticate("auth-jwt")
        {
            get("/hello"){
                //val userSession = call.principal<UserSession>()
                val principal = call.principal<JWTPrincipal>()
                println(principal)
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }

        }


        // Начальная страница
        get("/"){
            call.respondText("Hello!, ${call.principal<CustomPrincipal>()?.userName}!")
        }


        //
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

        //Проверка статуса сервера
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
            println("hello")
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
