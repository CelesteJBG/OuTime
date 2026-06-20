package com.outime.app.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CLIENT
)

enum class UserRole {
    CLIENT,
    BUSINESS
}