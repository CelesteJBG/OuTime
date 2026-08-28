package com.outime.app.domain.model

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val businessId: String = "",
    val businessName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val dateTime: Long = 0L,
    val status: AppointmentStatus = AppointmentStatus.CONFIRMED,
    val createdAt: Long = System.currentTimeMillis(),
    val servicePrice: Double = 0.0 // Snapshot del precio del servicio en el momento de crear la reserva.
)

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
