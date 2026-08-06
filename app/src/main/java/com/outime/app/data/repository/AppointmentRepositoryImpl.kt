package com.outime.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.domain.repository.AppointmentRepository
import kotlinx.coroutines.tasks.await

class AppointmentRepositoryImpl(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    companion object {
        private const val APPOINTMENTS_COLLECTION = "appointments"
    }

    override suspend fun createAppointment(appointment: Appointment): Result<Unit> = try {
        val docRef = firestore
            .collection(APPOINTMENTS_COLLECTION)
            .document()

        val appointmentWithId = appointment.copy(id = docRef.id)

        docRef.set(appointmentWithId).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAppointmentsByBusiness(businessId: String): Result<List<Appointment>> = try {
        val snapshot = firestore
            .collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("businessId", businessId)
            .get()
            .await()

        val appointments = snapshot.toObjects(Appointment::class.java)

        Result.success(appointments)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAppointmentsByBusinessAndDate(
        businessId: String,
        startOfDay: Long,
        endOfDay: Long
    ): Result<List<Appointment>> = try {
        val snapshot = firestore
            .collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("businessId", businessId)
            .whereGreaterThanOrEqualTo("dateTime", startOfDay)
            .whereLessThanOrEqualTo("dateTime", endOfDay)
            .get()
            .await()

        val appointments = snapshot.toObjects(Appointment::class.java)

        Result.success(appointments)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAppointmentsByClient(clientId: String): Result<List<Appointment>> = try {
        val snapshot = firestore
            .collection(APPOINTMENTS_COLLECTION)
            .whereEqualTo("clientId", clientId)
            .get()
            .await()

        val appointments = snapshot.toObjects(Appointment::class.java)

        Result.success(appointments)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment?> = try {
        val snapshot = firestore
            .collection(APPOINTMENTS_COLLECTION)
            .document(appointmentId)
            .get()
            .await()

        val appointment = snapshot.toObject(Appointment::class.java)

        Result.success(appointment)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): Result<Unit> = try {
        firestore
            .collection(APPOINTMENTS_COLLECTION)
            .document(appointmentId)
            .update("status", status.name)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}