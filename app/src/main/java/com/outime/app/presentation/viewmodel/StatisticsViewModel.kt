package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.Service
import com.outime.app.domain.repository.AppointmentRepository
import com.outime.app.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // Datos completos cargados una única vez; el cambio de periodo se calcula en local.
    private var allAppointments: List<Appointment> = emptyList()
    private var allServices: List<Service> = emptyList()

    /** Carga citas y servicios del negocio y calcula las estadísticas del periodo indicado. */
    fun load(businessId: String, period: StatPeriod = StatPeriod.MONTH) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val appointmentsResult = appointmentRepository.getAppointmentsByBusiness(businessId)
            val servicesResult = serviceRepository.getServicesByBusiness(businessId)

            val appointments = appointmentsResult.getOrNull()
            val services = servicesResult.getOrNull()

            if (appointments == null || services == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudieron cargar los datos de estadísticas"
                )
                return@launch
            }

            allAppointments = appointments
            allServices = services

            val stats = computeStatistics(allAppointments, allServices, period)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSuccess = true,
                selectedPeriod = period,
                statistics = stats
            )
        }
    }

    /** Recalcula las estadísticas en local (sin Firestore) al cambiar de periodo. */
    fun selectPeriod(period: StatPeriod) {
        val stats = computeStatistics(allAppointments, allServices, period)
        _uiState.value = _uiState.value.copy(selectedPeriod = period, statistics = stats)
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isSuccess = false,
            error = null
        )
    }
}
