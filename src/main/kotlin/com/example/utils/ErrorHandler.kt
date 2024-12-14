package com.example.utils


import io.ktor.server.application.*

sealed class ApplicationException(message: String) : Exception(message)
class ResourceNotFoundException(message: String) : ApplicationException(message)
class ValidationException(message: String) : ApplicationException(message)
class UnauthorizedException(message: String) : ApplicationException(message)

fun Application.configureErrorHandling() {
//    install(StatusPages) {
//        exception<ResourceNotFoundException> { call, cause ->
//            call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message))
//        }
//        exception<ValidationException> { call, cause ->
//            call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
//        }
//        exception<UnauthorizedException> { call, cause ->
//            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to cause.message))
//        }
//    }
} 