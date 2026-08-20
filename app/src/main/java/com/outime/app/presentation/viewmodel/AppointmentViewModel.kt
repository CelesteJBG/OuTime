package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.domain.repository.AppointmentRepository
import com.outime.app.domain.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    fun createAppointment(
        clientId: String,
        businessId: String,
        businessName: String,
        serviceId: String,
        serviceName: String,
        dateTime: Long,
        servicePrice: Double
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
                status = AppointmentStatus.CONFIRMED,
                servicePrice = servicePrice
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
                    // Resolver nombres de clientes de forma concurrente
                    val uniqueClientIds = appointments
                        .map { it.clientId }
                        .distinct()
                        .filter { it.isNotBlank() }

                    val clientNames = if (uniqueClientIds.isEmpty()) {
                        emptyMap()
                    } else {
                        uniqueClientIds.map { clientId ->
                            async {
                                val userResult = authRepository.getUserById(clientId)
                                val user = userResult.getOrNull()
                                if (user != null && user.name.isNotBlank()) {
                                    clientId to user.name
                                } else {
                                    null
                                }
                            }
                        }.awaitAll().filterNotNull().toMap()
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        appointments = appointments,
                        clientNames = clientNames
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

    fun loadAppointmentsByBusinessAndDate(
        businessId: String,
        startOfDay: Long,
        endOfDay: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = appointmentRepository.getAppointmentsByBusinessAndDate(
                businessId, startOfDay, endOfDay
            )

            result.fold(
                onSuccess = { appointments ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dayAppointments = appointments
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

    /**
     * Carga una cita escaneada por QR junto con el nombre de su cliente.
     * Reutiliza [AppointmentRepository.getAppointmentById] y, para mostrar el nombre,
     * [AuthRepository.getUserById]. El estado se expone en [AppointmentUiState.selectedAppointment]
     * y [AppointmentUiState.scannedClientName]. La validación de negocio/estado se hace en la UI
     * mediante la util pura [com.outime.app.presentation.util.evaluateScannedAppointment].
     */
    fun loadScannedAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedAppointment = null,
                scannedClientName = null
            )

            val result = appointmentRepository.getAppointmentById(appointmentId)

            result.fold(
                onSuccess = { appointment ->
                    var scannedClientName: String? = null
                    if (appointment?.clientId?.isNotBlank() == true) {
                        scannedClientName = authRepository.getUserById(appointment.clientId)
                            .getOrNull()
                            ?.takeIf { it.name.isNotBlank() }
                            ?.name
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedAppointment = appointment,
                        scannedClientName = scannedClientName
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
