package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Service

data class ServiceUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val services: List<Service> = emptyList()
)
