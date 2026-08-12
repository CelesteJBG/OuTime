package com.outime.app.domain.repository

import com.outime.app.domain.model.User
import com.outime.app.domain.model.UserRole

interface AuthRepository {

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit>

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit>

    fun logout()

    fun getCurrentUserId(): String?

    suspend fun getCurrentUser(): User?

    suspend fun getUserById(userId: String): Result<User?>
}
