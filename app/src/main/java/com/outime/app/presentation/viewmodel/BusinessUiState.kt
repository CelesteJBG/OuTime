package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Business

data class BusinessUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val business: Business? = null
)
