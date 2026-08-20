package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.model.Business
import com.outime.app.domain.repository.BusinessRepository
import com.outime.app.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessViewModel(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState: StateFlow<BusinessUiState> = _uiState.asStateFlow()

    fun createBusiness(
        ownerId: String,
        name: String,
        description: String,
        category: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Regla de producto: una cuenta BUSINESS tiene un único negocio.
            val existing = businessRepository.getBusinessByOwnerId(ownerId).getOrNull()
            if (existing != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    business = existing
                )
                return@launch
            }

            val business = Business(
                ownerId = ownerId,
                name = name,
                description = description,
                category = category
            )

            val result = businessRepository.createBusiness(business)

            result.fold(
                onSuccess = { createdBusiness ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        // El negocio recién creado (con su id real de Firestore) queda en el
                        // estado para que Home pueda cargar sus datos de inmediato.
                        business = createdBusiness
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

    fun loadBusinessByOwnerId(ownerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = businessRepository.getBusinessByOwnerId(ownerId)

            result.fold(
                onSuccess = { business ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        business = business
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

    fun loadBusinessById(businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = businessRepository.getBusinessById(businessId)

            result.fold(
                onSuccess = { business ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        business = business
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

    fun updateBusiness(business: Business) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = businessRepository.updateBusiness(business)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        business = business
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
    suspend fun getBusinessByOwnerId(ownerId: String): Business? {

        val business = businessRepository
            .getBusinessByOwnerId(ownerId)
            .getOrNull()

        _uiState.value = _uiState.value.copy(
            business = business
        )

        // Migración automática: si hay servicios guardados con ownerId en lugar de business.id
        if (business != null && business.id != ownerId) {
            serviceRepository.migrateServiceBusinessId(
                oldBusinessId = ownerId,
                newBusinessId = business.id
            )
        }

        return business
    }

    fun currentBusinessId(): String? {
        return _uiState.value.business?.id
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isSuccess = false,
            error = null
        )
    }
}
