package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Appointment

data class AppointmentUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val appointments: List<Appointment> = emptyList(),
    val dayAppointments: List<Appointment> = emptyList(),
    val selectedAppointment: Appointment? = null,
    val scannedClientName: String? = null,
    val clientNames: Map<String, String> = emptyMap()
)
