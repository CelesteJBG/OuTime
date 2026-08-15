package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Service

data class ServiceUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    // Servicios activos (los que se muestran y pueden reservarse).
    val services: List<Service> = emptyList(),
    // Todos los servicios del negocio (activos + inactivos). Se conserva para
    // métricas/ingresos de BusinessHome y futuras estadísticas sin romper las
    // citas históricas asociadas a servicios inactivos.
    val allServices: List<Service> = emptyList()
)
