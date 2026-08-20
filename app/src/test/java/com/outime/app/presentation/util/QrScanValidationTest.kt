package com.outime.app.presentation.util

import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas de la validación de una cita escaneada por QR.
 *
 * Regla de seguridad: solo las citas CONFIRMED y del mismo negocio pueden
 * procesarse/completarse mediante QR. PENDING, CANCELLED y COMPLETED no.
 */
class QrScanValidationTest {

    private fun appt(businessId: String, status: AppointmentStatus) = Appointment(
        id = "appt-1",
        businessId = businessId,
        status = status
    )

    @Test
    fun `cita inexistente no es una cita valida`() {
        assertEquals(ScanOutcome.NOT_AN_APPOINTMENT, evaluateScannedAppointment(null, "bizA"))
    }

    @Test
    fun `cita de otro negocio se rechaza`() {
        val appointment = appt("bizB", AppointmentStatus.CONFIRMED)
        assertEquals(ScanOutcome.OTHER_BUSINESS, evaluateScannedAppointment(appointment, "bizA"))
    }

    @Test
    fun `cita cancelada no se puede procesar`() {
        val appointment = appt("bizA", AppointmentStatus.CANCELLED)
        assertEquals(ScanOutcome.CANCELLED, evaluateScannedAppointment(appointment, "bizA"))
    }

    @Test
    fun `cita completada no se puede volver a completar`() {
        val appointment = appt("bizA", AppointmentStatus.COMPLETED)
        assertEquals(ScanOutcome.ALREADY_COMPLETED, evaluateScannedAppointment(appointment, "bizA"))
    }

    @Test
    fun `cita pendiente no se puede completar por QR`() {
        val appointment = appt("bizA", AppointmentStatus.PENDING)
        assertEquals(ScanOutcome.NOT_CONFIRMED, evaluateScannedAppointment(appointment, "bizA"))
    }

    @Test
    fun `cita confirmada del mismo negocio es procesable`() {
        val appointment = appt("bizA", AppointmentStatus.CONFIRMED)
        assertEquals(ScanOutcome.VALID, evaluateScannedAppointment(appointment, "bizA"))
    }
}