package com.outime.app.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CLIENT,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    CLIENT,
    BUSINESS
}