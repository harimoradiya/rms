package com.example.repositories

import com.example.database.DatabaseFactory
import com.example.database.DatabaseFactory.dbQuery
import com.example.database.UserTable
import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.mindrot.jbcrypt.BCrypt

class UserRepository {
    
    suspend fun createUser(request: UserRegistrationRequest): User = dbQuery {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        
        val insertStatement = UserTable.insert {
            it[username] = request.username
            it[email] = request.email
            it[password] = hashedPassword
            it[role] = request.role
        }
        
        val resultRow = insertStatement.resultedValues?.first()
        resultRow?.let { 
            User(
                id = it[UserTable.id],
                username = it[UserTable.username],
                email = it[UserTable.email],
                password = it[UserTable.password],
                role = it[UserTable.role],
                isActive = it[UserTable.isActive]
            )
        } ?: throw Exception("Failed to create user")
    }

    suspend fun getUserByUsername(username: String): User? = dbQuery {
        UserTable.select { UserTable.username eq username }
            .mapNotNull { row ->
                User(
                    id = row[UserTable.id],
                    username = row[UserTable.username],
                    email = row[UserTable.email],
                    password = row[UserTable.password],
                    role = row[UserTable.role],
                    isActive = row[UserTable.isActive]
                )
            }
            .singleOrNull()
    }

    suspend fun validateCredentials(username: String, password: String): User? {
        val user = getUserByUsername(username)
        return if (user != null && BCrypt.checkpw(password, user.password)) {
            user
        } else null
    }

    suspend fun getUserByEmail(email: String): User? = dbQuery {
        UserTable.select { UserTable.email eq email }
            .mapNotNull { row ->
                User(
                    id = row[UserTable.id],
                    username = row[UserTable.username],
                    email = row[UserTable.email],
                    password = row[UserTable.password],
                    role = row[UserTable.role],
                    isActive = row[UserTable.isActive]
                )
            }
            .singleOrNull()
    }
} 