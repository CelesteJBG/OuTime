package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.domain.model.DaySchedule
import com.outime.app.domain.repository.ScheduleRepository
import com.outime.app.presentation.model.TimeSlot
import com.outime.app.presentation.util.utcMidnight
import com.outime.app.presentation.util.utcMidnightOfLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Tests de la lógica de generación de franjas horarias [ScheduleViewModel.generateTimeSlots].
 *
 * La función es puramente computacional: recibe horario, fecha, duración, citas existentes
 * y fechas bloqueadas, y devuelve las franjas disponibles. No depende de Firebase ni de la UI.
 *
 * El ViewModel se instancia con un repositorio fake (vacío) porque generateTimeSlots()
 * no utiliza el repositorio; toda la lógica reside en la propia función.
 */
class ScheduleViewModelTest {

    /** Repositorio fake — ningún método será llamado por [generateTimeSlots]. */
    private val fakeRepo = object : ScheduleRepository {
        override suspend fun getSchedule(businessId: String) = Result.success(null)
        override suspend fun saveSchedule(schedule: BusinessSchedule) = Result.success(Unit)
        override suspend fun getBlockedDates(businessId: String) = Result.success(emptyList<BlockedDate>())
        override suspend fun addBlockedDate(blockedDate: BlockedDate) = Result.success(Unit)
        override suspend fun removeBlockedDate(blockedDateId: String) = Result.success(Unit)
        override fun observeSchedule(businessId: String): Flow<BusinessSchedule?> = emptyFlow()
        override fun observeBlockedDates(businessId: String): Flow<List<BlockedDate>> = emptyFlow()
    }

    private val viewModel = ScheduleViewModel(fakeRepo)
// ── Helpers ─────────────────────────────────────────────────────────────

    /** Fecha de test futura (Octubre 2026) para evitar interferencia con [System.currentTimeMillis]. */
    private val testYear = 2026
    private val testMonth = Calendar.OCTOBER
    private val testDay = 1
    private val testDateMillis: Long by lazy { localMidnight(testYear, testMonth, testDay) }

    /** Medianoche LOCAL de una fecha (mismo criterio que usa BookingScreen al llamar a generateTimeSlots). */
    private fun localMidnight(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Día de la semana (Calendar.DAY_OF_WEEK) para un instante dado. */
    private fun dayOfWeek(epochMillis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = epochMillis }
            .get(Calendar.DAY_OF_WEEK)

    /**
     * Construye un [BusinessSchedule] con un único día configurado.
     * El día de la semana se determina automáticamente a partir de [baseDateMillis].
     */
    private fun scheduleForDay(
        baseDateMillis: Long,
        isOpen: Boolean = true,
        morningStart: String = "",
        morningEnd: String = "",
        afternoonStart: String = "",
        afternoonEnd: String = ""
    ): BusinessSchedule {
        val dow = dayOfWeek(baseDateMillis)
        return BusinessSchedule(
            businessId = "test-biz",
            weeklyHours = mapOf(
                dow to DaySchedule(
                    isOpen = isOpen,
                    morningStart = morningStart,
                    morningEnd = morningEnd,
                    afternoonStart = afternoonStart,
                    afternoonEnd = afternoonEnd
                )
            )
        )
    }

    /** Cita de prueba con valores mínimos. */
    private fun appointment(
        dateTime: Long,
        clientId: String = "test-client",
        serviceName: String = "Test Service"
    ) = Appointment(
        id = "appt-$dateTime",
        clientId = clientId,
        businessId = "test-biz",
        serviceName = serviceName,
        dateTime = dateTime
    )

    // ── Tests ──────────────────────────────────────────────────────────────

    @Test
    fun `dia abierto solo manana genera franjas en manana`() {
        // Arrange: L-V 09:00-14:00, solo mañana
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = true,
            morningStart = "09:00",
            morningEnd = "14:00"
        )

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        // Assert
        assertTrue("Debería generar franjas para horario de mañana", slots.isNotEmpty())
        assertEquals("09:00-14:00 con duración 30 min → 10 franjas", 10, slots.size)
        assertTrue("Todas las franjas deberían estar disponibles", slots.all { it.isAvailable })
    }
    @Test
    fun `dia abierto solo tarde genera franjas en tarde`() {
        // Arrange: L-V 16:00-20:00, solo tarde
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = true,
            afternoonStart = "16:00",
            afternoonEnd = "20:00"
        )

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        // Assert
        assertTrue("Debería generar franjas para horario de tarde", slots.isNotEmpty())
        assertEquals("16:00-20:00 con duración 30 min → 8 franjas", 8, slots.size)
        assertTrue("Todas las franjas deberían estar disponibles", slots.all { it.isAvailable })
    }

    @Test
    fun `dia abierto manana y tarde genera franjas en ambos turnos`() {
        // Arrange: L-V 09:00-14:00 y 16:00-20:00
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = true,
            morningStart = "09:00",
            morningEnd = "14:00",
            afternoonStart = "16:00",
            afternoonEnd = "20:00"
        )

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        // Assert
        assertEquals("10 (mañana) + 8 (tarde) = 18 franjas", 18, slots.size)
        assertTrue("Todas las franjas deberían estar disponibles", slots.all { it.isAvailable })

        // Verificar separación: no hay franjas entre 14:00 y 16:00
        val morningEnd = localMidnight(testYear, testMonth, testDay) + 14 * 3600_000L
        val afternoonStart = localMidnight(testYear, testMonth, testDay) + 16 * 3600_000L

        val morningSlots = slots.filter { it.endMillis <= morningEnd }
        val afternoonSlots = slots.filter { it.startMillis >= afternoonStart }

        assertEquals("10 franjas en turno de mañana", 10, morningSlots.size)
        assertEquals("8 franjas en turno de tarde", 8, afternoonSlots.size)
        assertEquals("Suma debe coincidir con total", slots.size, morningSlots.size + afternoonSlots.size)
    }

    @Test
    fun `dia cerrado no genera ninguna franja`() {
        // Arrange: día con isOpen = false (aunque tenga horarios definidos)
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = false,
            morningStart = "09:00",
            morningEnd = "14:00"
        )

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        // Assert
        assertTrue("Día cerrado no debe generar franjas", slots.isEmpty())
    }

    @Test
    fun `fecha bloqueada no genera ninguna franja`() {
        // Arrange: día abierto pero con fecha bloqueada
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = true,
            morningStart = "09:00",
            morningEnd = "14:00"
        )

        // La fecha bloqueada debe almacenarse con la clave canónica UTC
        val blockedDate = BlockedDate(
            id = "block-1",
            businessId = "test-biz",
            date = utcMidnightOfLocalDate(testDateMillis)
        )

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = listOf(blockedDate)
        )

        // Assert
        assertTrue("Fecha bloqueada no debe generar franjas", slots.isEmpty())
    }

    @Test
    fun `cita existente marca franja solapada como no disponible`() {
        // Arrange: L-V 09:00-14:00, cita existente a las 10:00 (30 min)
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = true,
            morningStart = "09:00",
            morningEnd = "14:00"
        )

        val citaInicio = localMidnight(testYear, testMonth, testDay) + 10 * 3600_000L
        val citaExistente = appointment(dateTime = citaInicio)

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = listOf(citaExistente),
            blockedDates = emptyList()
        )

        // Assert
        assertEquals("Debería haber 10 franjas", 10, slots.size)

        // La franja de 10:00-10:30 debe estar no disponible
        val slot10 = slots.find { it.startMillis == citaInicio }
        assertNotNull("Debería existir franja a las 10:00", slot10)
        assertFalse("Franja solapada con cita no debe estar disponible", slot10!!.isAvailable)

        // El resto (9 franjas) deben estar disponibles
        val disponibles = slots.filter { it.isAvailable }
        assertEquals("9 franjas disponibles de 10", 9, disponibles.size)
    }

    @Test
    fun `duracion de servicio afecta numero de franjas generadas`() {
        // Arrange: L-V 09:00-14:00
        val schedule = scheduleForDay(
            baseDateMillis = testDateMillis,
            isOpen = true,
            morningStart = "09:00",
            morningEnd = "14:00"
        )

        // Act: 30 min vs 60 min
        val slots30 = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        val slots60 = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = testDateMillis,
            durationMinutes = 60,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        // Assert
        assertEquals("30 min → 10 franjas en 5 horas", 10, slots30.size)
        assertEquals("60 min → 5 franjas en 5 horas", 5, slots60.size)
        assertTrue("Franjas de 30 min deben durar exactamente 30 min",
            slots30.all { it.endMillis - it.startMillis == 30 * 60_000L })
    }

    @Test
    fun `fecha pasada marca todas las franjas como no disponibles`() {
        // Arrange: fecha en el pasado remoto (año 2000).
        // System.currentTimeMillis() siempre será > 2000, por lo que el
        // resultado es determinista: todas las franjas aparecen como pasadas.
        val pastDate = localMidnight(2000, Calendar.JANUARY, 3)
        val pastDow = dayOfWeek(pastDate)

        val schedule = BusinessSchedule(
            businessId = "test-biz",
            weeklyHours = mapOf(
                pastDow to DaySchedule(
                    isOpen = true,
                    morningStart = "09:00",
                    morningEnd = "14:00"
                )
            )
        )

        // Act
        val slots = viewModel.generateTimeSlots(
            schedule = schedule,
            dateMillis = pastDate,
            durationMinutes = 30,
            existingAppointments = emptyList(),
            blockedDates = emptyList()
        )

        // Assert
        assertTrue("El horario es válido, debe generar franjas", slots.isNotEmpty())
        assertTrue("Todas las franjas pasadas deben estar no disponibles",
            slots.none { it.isAvailable })
    }
}