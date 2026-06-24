package com.outime.app.domain.repository

import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus

interface AppointmentRepository {

    suspend fun createAppointment(appointment: Appointment): Result<Unit>

    suspend fun getAppointmentsByBusiness(businessId: String): Result<List<Appointment>>

    suspend fun getAppointmentsByClient(clientId: String): Result<List<Appointment>>

    suspend fun getAppointmentById(appointmentId: String): Result<Appointment?>

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): Result<Unit>
}
