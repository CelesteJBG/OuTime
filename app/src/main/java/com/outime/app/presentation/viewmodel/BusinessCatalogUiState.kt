package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Business
import com.outime.app.domain.model.Service

data class BusinessCatalogUiState(
    val isLoading: Boolean = false,
    val businesses: List<Business> = emptyList(),
    val filteredBusinesses: List<Business> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedBusiness: Business? = null,
    val selectedServices: List<Service> = emptyList(),
    val error: String? = null
)
