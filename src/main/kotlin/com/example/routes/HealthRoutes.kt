package com.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes() {
    get("/health") {
        try {
            // Add actual health checks here (DB connection, services, etc.)
            val healthStatus = mapOf(
                "status" to "UP",
                "database" to "UP",
                "timestamp" to System.currentTimeMillis()
            )
            call.respond(HttpStatusCode.OK, healthStatus)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                mapOf("status" to "DOWN", "error" to e.message)
            )
        }
    }
} 