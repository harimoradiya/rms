package com.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.User
import java.util.*

object JWTConfig {
    private const val SECRET = "your-secret-key" // In production, use environment variable
    private const val ISSUER = "restaurant-management-system"
    private const val VALIDITY_IN_MS = 36_000_00 * 24 // 24 hours

    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(ISSUER)
        .withClaim("id", user.id)
        .withClaim("username", user.username)
        .withClaim("role", user.role.name)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
        .sign(Algorithm.HMAC256(SECRET))

    fun verifyToken(token: String) = JWT.require(Algorithm.HMAC256(SECRET))
        .withIssuer(ISSUER)
        .build()
        .verify(token)
} 