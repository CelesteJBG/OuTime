package com.outime.app.domain.model

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val businessId: String = "",
    val businessName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val dateTime: Long = 0L,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
