package com.example
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.digest
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

@Serializable
data class UserSession(val name:String, val count: Int)


fun Application.configureAuth()
{


    var secret = environment.config.property("jwt.secret").getString()
    var issuer = environment.config.property("jwt.issuer").getString()
    var audience = environment.config.property("jwt.audience").getString()



    install(Sessions){
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60
        }

    }

    var myRealm = environment.config.property("jwt.realm").getString()
    install(Authentication)
    {

//        session<UserSession>("auth-session"){
//            validate { session ->
//                if(session.name.startsWith("jet")){
//                    session
//                }else{
//                    null
//                }
//            }
//            challenge{
//                call.respondRedirect("/login")
//            }
//
//        }




        jwt("auth-jwt") {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())
            validate { credential ->
                if(credential.payload.getClaim("username").asString() !=""){
                    JWTPrincipal(credential.payload)
                }else{
                    null
                }
            }
            challenge{defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }




//        digest("auth-digest") {
//            realm = myRealm
//            digestProvider{ userName, realm ->
//                userTable[userName]
//            }
//            validate { credentials ->
//                if(credentials.userName.isNotEmpty()){
//                CustomPrincipal(credentials.userName, credentials.realm)
//                }else{
//                    null
//                }
//                }
//        }

    }

}

data class CustomPrincipal(val userName: String, val realm: String)
