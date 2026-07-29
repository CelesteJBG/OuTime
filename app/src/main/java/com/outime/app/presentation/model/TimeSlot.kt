package com.outime.app.presentation.model

/**
 * Franja horaria generada dinámicamente para un día concreto.
 *
 * - [startMillis] / [endMillis]: instante de inicio y fin de la franja (epoch ms).
 * - [isAvailable]: false si la franja coincide con una cita existente o no cumple
 *   las validaciones de disponibilidad.
 */
data class TimeSlot(
    val startMillis: Long,
    val endMillis: Long,
    val isAvailable: Boolean
)