package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.repository.BusinessRepository
import com.outime.app.domain.repository.ServiceRepository
import com.outime.app.presentation.util.normalizeText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessCatalogViewModel(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessCatalogUiState())
    val uiState: StateFlow<BusinessCatalogUiState> = _uiState.asStateFlow()

    init {
        loadBusinesses()
    }

    fun loadBusinesses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = businessRepository.getAllBusinesses()

            result.fold(
                onSuccess = { businesses ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        businesses = businesses,
                        filteredBusinesses = applyFilters(
                            businesses,
                            _uiState.value.searchQuery,
                            _uiState.value.selectedCategory
                        )
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredBusinesses = applyFilters(
                _uiState.value.businesses,
                query,
                _uiState.value.selectedCategory
            )
        )
    }

    fun onCategorySelected(category: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredBusinesses = applyFilters(
                _uiState.value.businesses,
                _uiState.value.searchQuery,
                category
            )
        )
    }

    fun loadBusinessDetail(businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val businessResult = businessRepository.getBusinessById(businessId)

            businessResult.fold(
                onSuccess = { business ->
                    _uiState.value = _uiState.value.copy(
                        selectedBusiness = business
                    )

                    val servicesResult = serviceRepository.getServicesByBusiness(businessId)

                    servicesResult.fold(
                        onSuccess = { services ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                // Solo los activos son seleccionables/reservables.
                                selectedServices = services.filter { it.isActive }
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    private fun applyFilters(
        businesses: List<com.outime.app.domain.model.Business>,
        query: String,
        category: String?
    ): List<com.outime.app.domain.model.Business> {
        val normalizedQuery = normalizeText(query)

        return businesses.filter { business ->
            val matchesQuery = normalizedQuery.isBlank() ||
                normalizeText(business.name).contains(normalizedQuery) ||
                normalizeText(business.description).contains(normalizedQuery)

            // Comparación por categoría normalizada (sin mayúsculas/acentos/espacios).
            val matchesCategory = category == null ||
                normalizeText(business.category) == normalizeText(category)

            matchesQuery && matchesCategory
        }
    }
}
