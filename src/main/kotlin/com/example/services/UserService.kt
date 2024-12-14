package com.example.services

import com.example.models.*
import com.example.repositories.UserRepository
import com.example.utils.JWTConfig
import org.mindrot.jbcrypt.BCrypt

class UserService(private val userRepository: UserRepository) {

    suspend fun registerUser(request: RegisterRequest): AuthResponse {
        // Check if user already exists
        if (userRepository.getUserByEmail(request.email) != null) {
            throw Exception("User with this email already exists")
        }

        // Create user
        val user = UserRegistrationRequest(
            username = request.name,
            email = request.email,
            password = BCrypt.hashpw(request.password, BCrypt.gensalt()),
            role = request.role
        )

        val createdUser = userRepository.createUser(user)
        val token = JWTConfig.generateToken(createdUser)

        return AuthResponse(
            token = token,
            user = UserDTO(
                id = createdUser.id,
                email = createdUser.email,
                name = createdUser.username,
                role = createdUser.role
            )
        )
    }

    suspend fun loginUser(request: LoginRequest): AuthResponse {
        // Get user by email
        val user = userRepository.getUserByEmail(request.email)
            ?: throw Exception("Invalid email or password")

        // Verify password
        if (!BCrypt.checkpw(request.password, user.password)) {
            throw Exception("Invalid email or password")
        }

        // Generate token
        val token = JWTConfig.generateToken(user)

        return AuthResponse(
            token = token,
            user = UserDTO(
                id = user.id,
                email = user.email,
                name = user.username,
                role = user.role
            )
        )
    }
} 