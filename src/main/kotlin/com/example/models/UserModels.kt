package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val isActive: Boolean = true
)

@Serializable
data class UserLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserRegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole = UserRole.CUSTOMER
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDTO
)

@Serializable
data class UserDTO(
    val id: Int,
    val email: String,
    val name: String,
    val role: UserRole
) 