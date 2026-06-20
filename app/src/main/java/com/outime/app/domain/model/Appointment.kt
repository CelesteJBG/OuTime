package com.outime.app.domain.model

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val businessId: String = "",
    val serviceId: String = "",
    val dateTime: Long = 0L,
    val status: AppointmentStatus = AppointmentStatus.PENDING
)

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}