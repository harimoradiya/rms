package com.example.routes

import com.example.models.*
import com.example.repositories.UserRepository
import com.example.services.UserService
import com.example.utils.JWTConfig
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route

class AuthRoutes(private val userService: UserService) {

    fun Route.authRoutes() {
        route("/auth") {
            post("/register", {
                tags = listOf("Authentication")
                summary = "Register new user"
                description = "Create a new user account"
                request {
                    body<RegisterRequest>()
                }
                response {
                    HttpStatusCode.Created to {
                        description = "User registered successfully"
                        body<AuthResponse>()
                    }
                }
            }) {
                try {
                    val request = call.receive<RegisterRequest>()
                    val response = userService.registerUser(request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Registration failed"))
                    )
                }
            }

            post("/login", {
                tags = listOf("Authentication")
                summary = "User login"
                description = "Authenticate user and get JWT token"
                request {
                    body<LoginRequest>()
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Login successful"
                        body<AuthResponse>()
                    }
                }
            }) {
                try {
                    val request = call.receive<LoginRequest>()
                    val response = userService.loginUser(request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to (e.message ?: "Login failed"))
                    )
                }
            }
        }
    }
} 