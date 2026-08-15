package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.model.Service
import com.outime.app.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceViewModel(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    fun createService(
        businessId: String,
        name: String,
        description: String,
        durationMinutes: Int,
        price: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val service = Service(
                businessId = businessId,
                name = name,
                description = description,
                durationMinutes = durationMinutes,
                price = price,
                isActive = true
            )

            val result = serviceRepository.createService(service)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
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

    fun updateService(
        serviceId: String,
        businessId: String,
        name: String,
        description: String,
        durationMinutes: Int,
        price: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val current = _uiState.value.services
                .firstOrNull { it.id == serviceId }

            val service = Service(
                id = serviceId,
                businessId = businessId,
                name = name,
                description = description,
                durationMinutes = durationMinutes,
                price = price,
                isActive = current?.isActive ?: true
            )

            val result = serviceRepository.updateService(service)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
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

    /** Baja lógica: marca el servicio inactivo y recarga la lista. */
    fun deactivateService(serviceId: String, businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = serviceRepository.deactivateService(serviceId)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    // El servicio desaparece de la lista de activos.
                    if (businessId.isNotEmpty()) {
                        loadServices(businessId)
                    }
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

    fun loadServices(businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = serviceRepository.getServicesByBusiness(businessId)

            result.fold(
                onSuccess = { services ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        // Se conservan todos para ingresos/estadísticas.
                        allServices = services,
                        // Solo se muestran/reservan los activos.
                        services = services.filter { it.isActive }
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

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isSuccess = false,
            error = null
        )
    }
}
