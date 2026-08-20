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
        // ⚠️ TODO (validación atómica a futuro — Race condition)
        // ------------------------------------------------------------------
        // Hoy la cita se crea sin comprobar de forma atómica que la fecha
        // siga disponible. Aunque la UI (BookingScreen) ya revalida y escucha
        // fechas bloqueadas en tiempo real, existe una ventana de concurrencia:
        // el negocio puede bloquear una fecha (o apagar un día) justo mientras
        // el cliente confirma, y esta escritura la aceptaría igual.
        //
        // Corrección recomendada (más robusta, nivel servidor y atómica):
        //   1) Envolver esta escritura en una transacción de Firestore
        //      (`firestore.runTransaction`) que:
        //        - lea el documento `business_schedules/{appointment.businessId}` y
        //          rechace si ese día de la semana está apagado (`isOpen == false`,
        //          turnos en blanco o documento inexistente);
        //        - compruebe en `blocked_dates` (filtrado por businessId) si el día
        //          (medianoche local de `appointment.dateTime`) figura como bloqueado
        //          y rechace en ese caso;
        //        - compruebe que la franja no se solape con otra cita CONFIRMED
        //          del negocio para esa fecha (evita dobles reservas simultáneas);
        //        - y solo entonces realice el `set` de la cita.
        //   2) Opcional / segunda capa de seguridad: añadir una regla de seguridad
        //      en `firestore.rules` que impida una escritura en `appointments` si la
        //      fecha está en `blocked_dates` o el día no está abierto en
        //      `business_schedules`, de modo que el backend bloquee la carrera aunque
        //      una app no actualizada intente reservar.
        // ------------------------------------------------------------------
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