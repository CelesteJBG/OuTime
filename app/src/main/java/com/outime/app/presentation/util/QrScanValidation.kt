package com.outime.app.presentation.util

import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus

/**
 * Resultado de evaluar una cita escaneada mediante QR.
 *
 * Solo las citas CONFIRMED pueden procesarse/completarse por QR. PENDING,
 * CANCELLED y COMPLETED no son procesables (ajuste de seguridad del flujo QR).
 */
enum class ScanOutcome {
    /** La cita existe, pertenece a este negocio y está CONFIRMED: procesable. */
    VALID,

    /** El ID escaneado no corresponde a una cita existente (o el QR no es válido). */
    NOT_AN_APPOINTMENT,

    /** La cita pertenece a otro negocio: la app debe rechazarla. */
    OTHER_BUSINESS,

    /** La cita está CANCELLED: no procesable. */
    CANCELLED,

    /** La cita ya está COMPLETED: no se puede completar de nuevo. */
    ALREADY_COMPLETED,

    /** La cita está PENDING: solo las CONFIRMED pueden completarse por QR. */
    NOT_CONFIRMED
}

/**
 * Valida una cita obtenida de Firestore tras escanear un QR.
 *
 * @param appointment       cita recuperada por [Appointment.id]; null si no existe.
 * @param currentBusinessId businessId del negocio autenticado; si no coincide, se rechaza.
 */
fun evaluateScannedAppointment(
    appointment: Appointment?,
    currentBusinessId: String
): ScanOutcome {
    val appointmentValue = appointment ?: return ScanOutcome.NOT_AN_APPOINTMENT

    if (appointmentValue.businessId != currentBusinessId) return ScanOutcome.OTHER_BUSINESS

    return when (appointmentValue.status) {
        AppointmentStatus.CANCELLED -> ScanOutcome.CANCELLED
        AppointmentStatus.COMPLETED -> ScanOutcome.ALREADY_COMPLETED
        AppointmentStatus.PENDING -> ScanOutcome.NOT_CONFIRMED
        AppointmentStatus.CONFIRMED -> ScanOutcome.VALID
    }
}