package com.outime.app.presentation.util

import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.domain.model.Service
import com.outime.app.presentation.viewmodel.StatPeriod
import com.outime.app.presentation.viewmodel.computeStatistics
import org.junit.Assert
import org.junit.Test
import java.util.Calendar

/**
 * Tests JVM de la lógica pura de estadísticas (computeStatistics).
 * El "instante de referencia" se fija para que los periodos sean deterministas.
 */
class StatisticsCalculatorTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Sábado 15 de junio de 2024 (semana 10–16 jun, mes junio).
    private val REFERENCE = at(2024, Calendar.JUNE, 15, 12)

    private fun appt(
        status: AppointmentStatus,
        serviceId: String = "s1",
        serviceName: String = "Corte",
        dateTime: Long = REFERENCE,
        clientId: String = "c1",
        servicePrice: Double = 0.0
    ) = Appointment(
        id = "id-$status-$servicePrice",
        clientId = clientId,
        serviceId = serviceId,
        serviceName = serviceName,
        dateTime = dateTime,
        status = status,
        servicePrice = servicePrice
    )

    private fun service(
        id: String = "s1",
        price: Double,
        isActive: Boolean = true
    ) = Service(id = id, name = "Servicio $id", price = price, isActive = isActive)

    // ── Ingresos ──────────────────────────────────────────────
    @Test
    fun `ingresos suman solo confirmadas y completadas`() {
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, servicePrice = 15.0),
            appt(AppointmentStatus.CONFIRMED, servicePrice = 20.0),
            appt(AppointmentStatus.CANCELLED, servicePrice = 100.0), // no cuenta
            appt(AppointmentStatus.PENDING, servicePrice = 100.0)    // no cuenta
        )
        val services = listOf(service(price = 20.0))

        val stats = computeStatistics(appointments, services, StatPeriod.ALL, REFERENCE)

        Assert.assertEquals(35.0, stats.revenue, 0.0001)
    }

    @Test
    fun `citas canceladas no generan ingresos`() {
        val appointments = listOf(
            appt(AppointmentStatus.CANCELLED, servicePrice = 99.0),
            appt(AppointmentStatus.CANCELLED, servicePrice = 99.0)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(0.0, stats.revenue, 0.0001)
    }

    // ── Citas completadas ─────────────────────────────────────
    @Test
    fun `citas completadas cuentan solo COMPLETED`() {
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED),
            appt(AppointmentStatus.COMPLETED),
            appt(AppointmentStatus.CONFIRMED),
            appt(AppointmentStatus.CANCELLED)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(2, stats.completedCount)
    }

    // ── Clientes únicos atendidos ─────────────────────────────
    @Test
    fun `clientes atendidos son clientes unicos con cita COMPLETED`() {
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, clientId = "clienteA"),
            appt(AppointmentStatus.COMPLETED, clientId = "clienteA"), // repetido
            appt(AppointmentStatus.COMPLETED, clientId = "clienteB"),
            appt(AppointmentStatus.CONFIRMED, clientId = "clienteC")  // no atendido
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(2, stats.uniqueClientsServed)
    }

    // ── Tasa de cancelación ───────────────────────────────────
    @Test
    fun `tasa de cancelacion es canceladas sobre total`() {
        val appointments = listOf(
            appt(AppointmentStatus.CANCELLED),
            appt(AppointmentStatus.COMPLETED),
            appt(AppointmentStatus.COMPLETED),
            appt(AppointmentStatus.CONFIRMED)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(25.0, stats.cancellationRate, 0.0001)
    }

    @Test
    fun `tasa de cancelacion es 0 sin citas sin division por cero`() {
        val stats = computeStatistics(emptyList(), emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(0.0, stats.cancellationRate, 0.0001)
    }

    // ── Top 3 servicios ───────────────────────────────────────
    @Test
    fun `top 3 servicios ordenados por numero de citas e incluye inactivos`() {
        // "Corte" desactivado posteriormente pero con citas históricas.
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, serviceName = "Corte"),
            appt(AppointmentStatus.COMPLETED, serviceName = "Corte"),
            appt(AppointmentStatus.COMPLETED, serviceName = "Corte"),
            appt(AppointmentStatus.COMPLETED, serviceName = "Tinte"),
            appt(AppointmentStatus.COMPLETED, serviceName = "Tinte"),
            appt(AppointmentStatus.COMPLETED, serviceName = "Peluqueria"),
            appt(AppointmentStatus.COMPLETED, serviceName = "Manicura")
        )
        val services = listOf(
            service("s1", price = 20.0, isActive = false),
            service("s2", price = 20.0, isActive = true),
            service("s3", price = 20.0, isActive = true),
            service("s4", price = 20.0, isActive = true)
        )

        val stats = computeStatistics(appointments, services, StatPeriod.ALL, REFERENCE)

        Assert.assertEquals(3, stats.topServices.size)
        Assert.assertEquals("Corte", stats.topServices[0].name)
        Assert.assertEquals(3, stats.topServices[0].count)
        // El servicio inactivo (s1 "Corte") sigue apareciendo en el histórico.
        Assert.assertTrue(stats.topServices.any { it.name == "Corte" })
    }

    // ── Periodos (dateTime, no createdAt) ─────────────────────
    @Test
    fun `periodo Hoy usa solo citas del mismo dia`() {
        val sameDay = at(2024, Calendar.JUNE, 15, 9)
        val previousDay = at(2024, Calendar.JUNE, 14, 9)
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, dateTime = sameDay, servicePrice = 10.0),
            appt(AppointmentStatus.COMPLETED, dateTime = previousDay, servicePrice = 99.0)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.TODAY, REFERENCE)
        Assert.assertEquals(1, stats.totalAppointments)
        Assert.assertEquals(10.0, stats.revenue, 0.0001)
    }

    @Test
    fun `periodo Mes usa solo citas del mes`() {
        val thisMonth = at(2024, Calendar.JUNE, 1, 9)
        val previousMonth = at(2024, Calendar.MAY, 25, 9)
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, dateTime = thisMonth, servicePrice = 5.0),
            appt(AppointmentStatus.COMPLETED, dateTime = previousMonth, servicePrice = 99.0)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.MONTH, REFERENCE)
        Assert.assertEquals(1, stats.totalAppointments)
        Assert.assertEquals(5.0, stats.revenue, 0.0001)
    }

    @Test
    fun `periodo Semana usa solo citas de la semana`() {
        // Miércoles 12 (dentro de la semana 10–16) y domingo 9 (semana anterior).
        val inWeek = at(2024, Calendar.JUNE, 12, 9)
        val beforeWeek = at(2024, Calendar.JUNE, 9, 9)
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, dateTime = inWeek, servicePrice = 7.0),
            appt(AppointmentStatus.COMPLETED, dateTime = beforeWeek, servicePrice = 99.0)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.WEEK, REFERENCE)
        Assert.assertEquals(1, stats.totalAppointments)
        Assert.assertEquals(7.0, stats.revenue, 0.0001)
    }

    @Test
    fun `periodo Total no filtra`() {
        val appointments = listOf(
            appt(AppointmentStatus.COMPLETED, dateTime = REFERENCE, servicePrice = 3.0),
            appt(AppointmentStatus.COMPLETED, dateTime = at(2023, Calendar.JANUARY, 1, 9), servicePrice = 4.0)
        )
        val stats = computeStatistics(appointments, emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(2, stats.totalAppointments)
        Assert.assertEquals(7.0, stats.revenue, 0.0001)
    }

    // ── Snapshot histórico del precio (crítico) ───────────────
    @Test
    fun `cambio posterior del precio del servicio no altera el precio historico`() {
        // La cita se creó cuando el servicio costaba 15 € y guardó servicePrice = 15.
        val appointment = appt(AppointmentStatus.COMPLETED, servicePrice = 15.0)
        // Posteriormente el negocio sube el precio del servicio a 20 €.
        val services = listOf(service(price = 20.0))

        val stats = computeStatistics(listOf(appointment), services, StatPeriod.ALL, REFERENCE)

        // El ingreso debe usar 15 € (snapshot), NO 20 €.
        Assert.assertEquals(15.0, stats.revenue, 0.0001)
    }

    @Test
    fun `cita antigua sin servicePrice usa el precio actual como fallback`() {
        // Cita legada: servicePrice == 0.0 (el campo no existía en Firestore).
        val appointment = appt(AppointmentStatus.COMPLETED, servicePrice = 0.0)
        val services = listOf(service(price = 20.0))

        val stats = computeStatistics(listOf(appointment), services, StatPeriod.ALL, REFERENCE)

        Assert.assertEquals(20.0, stats.revenue, 0.0001)
    }

    // ── Ausencia total de datos ───────────────────────────────
    @Test
    fun `sin datos devuelve ceros y ultimos 7 dias en cero`() {
        val stats = computeStatistics(emptyList(), emptyList(), StatPeriod.ALL, REFERENCE)
        Assert.assertEquals(0.0, stats.revenue, 0.0001)
        Assert.assertEquals(0, stats.completedCount)
        Assert.assertEquals(0, stats.uniqueClientsServed)
        Assert.assertEquals(0.0, stats.cancellationRate, 0.0001)
        Assert.assertEquals(0, stats.topServices.size)
        Assert.assertEquals(7, stats.last7DaysRevenue.size)
        Assert.assertTrue(stats.last7DaysRevenue.all { it.revenue == 0.0 })
    }
}