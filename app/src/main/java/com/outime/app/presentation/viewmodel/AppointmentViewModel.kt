package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    fun createAppointment(
        clientId: String,
        businessId: String,
        businessName: String,
        serviceId: String,
        serviceName: String,
        dateTime: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val appointment = Appointment(
                clientId = clientId,
                businessId = businessId,
                businessName = businessName,
                serviceId = serviceId,
                serviceName = serviceName,
                dateTime = dateTime,
                status = AppointmentStatus.PENDING
            )

            val result = appointmentRepository.createAppointment(appointment)

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

    fun loadAppointmentsByBusiness(businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = appointmentRepository.getAppointmentsByBusiness(businessId)

            result.fold(
                onSuccess = { appointments ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = appointments
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

    fun loadAppointmentsByClient(clientId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = appointmentRepository.getAppointmentsByClient(clientId)

            result.fold(
                onSuccess = { appointments ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = appointments
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

    fun loadAppointmentById(appointmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = appointmentRepository.getAppointmentById(appointmentId)

            result.fold(
                onSuccess = { appointment ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedAppointment = appointment
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

    fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = appointmentRepository.updateAppointmentStatus(appointmentId, status)

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

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isSuccess = false,
            error = null
        )
    }
}
