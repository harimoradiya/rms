package com.example
import com.example.database.DatabaseFactory
import com.example.routes.configureRouting
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.config.configLoaders
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.example.utils.JWTConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.routes.TableRoutes
import com.example.repositories.TableRepository

fun main(){
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080){
        module()
    }.start(wait = true)
}


fun Application.module(){
    // Initialize database
    DatabaseFactory.init()
    
    install(ContentNegotiation){
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }
    install(SwaggerUI) {
        swagger {

//            swaggerUrl = "swagger-ui"
//            forwardRoot = true
        }
        info {
            title = "Restaurant Management System API"
            version = "1.0"
            description = "Comprehensive API for Restaurant Management"
            contact {
                name = "Restrofy"
                email = "support@restrofy.com"
            }
        }
    }

    install(Authentication) {
        jwt {
            val jwtAudience = "jwt-audience"
            realm = "ktor sample app"
            verifier(
                JWT
                    .require(Algorithm.HMAC256("secret"))
                    .withAudience(jwtAudience)
                    .withIssuer("restaurant-management-system")
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    configureRouting()
}



