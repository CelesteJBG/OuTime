package com.outime.app.presentation.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val isResetSent: Boolean = false
)