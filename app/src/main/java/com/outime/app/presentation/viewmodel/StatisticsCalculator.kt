package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.domain.model.Service
import java.util.Calendar

/**
 * Lógica pura de cálculo de estadísticas de negocio.
 *
 * Independiente de Android/Firebase: solo recibe listas y un periodo, para poder
 * probarla en tests JVM de forma aislada y reutilizarla desde la pantalla.
 */
enum class StatPeriod(val label: String) {
    TODAY("Hoy"),
    WEEK("Semana"),
    MONTH("Mes"),
    ALL("Total")
}

/** Ingresos agregados por día, para el gráfico de tendencia. */
data class DailyRevenue(val dateMillis: Long, val revenue: Double)

/** Conteo de citas por servicio (para el Top 3). */
data class ServiceCount(val name: String, val count: Int)

/** Resultado agregado de todos los cálculos de estadísticas. */
data class StatisticsData(
    val revenue: Double = 0.0,            // Ingresos previstos (CONFIRMED + COMPLETED)
    val completedCount: Int = 0,          // Citas completadas
    val uniqueClientsServed: Int = 0,     // Clientes únicos atendidos (solo COMPLETED)
    val cancellationRate: Double = 0.0,   // % de cancelación sobre el total del periodo
    val totalAppointments: Int = 0,       // Total de citas del periodo
    val topServices: List<ServiceCount> = emptyList(), // Top 3 servicios más solicitados
    val activeServicesCount: Int = 0,     // Nº de servicios activos
    val last7DaysRevenue: List<DailyRevenue> = emptyList() // Ingresos últimos 7 días
)

/**
 * Calcula todas las métricas de estadísticas a partir de las citas y servicios del negocio.
 *
 * [referenceTimeMillis] permite fijar el "ahora" en los tests para que los periodos
 * (Hoy/Semana/Mes) sean deterministas. Por defecto usa la hora actual del dispositivo.
 */
fun computeStatistics(
    appointments: List<Appointment>,
    services: List<Service>,
    period: StatPeriod,
    referenceTimeMillis: Long = System.currentTimeMillis()
): StatisticsData {
    // Precio por serviceId solo como fallback para citas antiguas con servicePrice == 0.
    val priceByServiceId = services.associate { it.id to it.price }

    // Precio de una cita: snapshot histórico si existe, si no el precio actual del servicio.
    fun revenueOf(appointment: Appointment): Double =
        appointment.servicePrice.takeIf { it > 0.0 } ?: (priceByServiceId[appointment.serviceId] ?: 0.0)

    val inPeriod = periodIn(period, referenceTimeMillis)
    val periodAppointments = appointments.filter(inPeriod)

    val completed = periodAppointments.filter { it.status == AppointmentStatus.COMPLETED }
    val cancelledCount = periodAppointments.count { it.status == AppointmentStatus.CANCELLED }

    val revenue = periodAppointments
        .filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.COMPLETED }
        .sumOf { revenueOf(it) }

    val uniqueClientsServed = completed
        .map { it.clientId }
        .filter { it.isNotBlank() }
        .distinct()
        .size

    val total = periodAppointments.size
    val cancellationRate = if (total == 0) 0.0 else cancelledCount.toDouble() / total * 100.0

    val topServices = periodAppointments
        .groupingBy { it.serviceName.ifBlank { it.serviceId }.ifBlank { "Sin servicio" } }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(3)
        .map { ServiceCount(it.first, it.second) }

    val activeServicesCount = services.count { it.isActive }

    return StatisticsData(
        revenue = revenue,
        completedCount = completed.size,
        uniqueClientsServed = uniqueClientsServed,
        cancellationRate = cancellationRate,
        totalAppointments = total,
        topServices = topServices,
        activeServicesCount = activeServicesCount,
        last7DaysRevenue = last7DaysRevenue(appointments, services, referenceTimeMillis)
    )
}

/** Ingresos (CONFIRMED + COMPLETED) de los últimos 7 días terminando en la fecha de [reference]. */
private fun last7DaysRevenue(
    appointments: List<Appointment>,
    services: List<Service>,
    referenceTimeMillis: Long
): List<DailyRevenue> {
    val priceByServiceId = services.associate { it.id to it.price }
    fun revenueOf(appointment: Appointment): Double =
        appointment.servicePrice.takeIf { it > 0.0 } ?: (priceByServiceId[appointment.serviceId] ?: 0.0)

    val todayStart = startOfDay(referenceTimeMillis)

    val days = (6 downTo 0).map { offset ->
        DailyRevenue(dateMillis = startOfDay(todayStart - offset * DAY_MILLIS), revenue = 0.0)
    }
    val dayIndexes = days.mapIndexed { index, day -> day.dateMillis to index }.toMap()
    val revenueByDay = DoubleArray(days.size)

    appointments.forEach { appointment ->
        if (appointment.status == AppointmentStatus.CONFIRMED || appointment.status == AppointmentStatus.COMPLETED) {
            val dayStart = startOfDay(appointment.dateTime)
            dayIndexes[dayStart]?.let { index ->
                revenueByDay[index] += revenueOf(appointment)
            }
        }
    }

    return days.mapIndexed { index, day ->
        DailyRevenue(day.dateMillis, revenueByDay[index])
    }
}

/** Devuelve true si la cita pertenece al periodo indicado (comparando por dateTime). */
private fun periodIn(period: StatPeriod, referenceTimeMillis: Long): (Appointment) -> Boolean {
    if (period == StatPeriod.ALL) return { true }

    val start = startOfDay(referenceTimeMillis)
    val (from, to) = when (period) {
        StatPeriod.TODAY -> start to (start + DAY_MILLIS - 1L)
        StatPeriod.WEEK -> {
            val weekStart = startOfDay(referenceTimeMillis - dayOfWeekOffset(referenceTimeMillis) * DAY_MILLIS)
            weekStart to (weekStart + 7L * DAY_MILLIS - 1L)
        }
        StatPeriod.MONTH -> {
            val monthStart = monthStartMillis(referenceTimeMillis)
            monthStart to (addMonthsMillis(monthStart, 1) - 1L)
        }
        StatPeriod.ALL -> Long.MIN_VALUE to Long.MAX_VALUE
    }

    return { appointment -> appointment.dateTime in from..to }
}

private fun startOfDay(timeMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timeMillis
    setStartOfDay(cal)
    return cal.timeInMillis
}

private fun setStartOfDay(cal: Calendar) {
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
}

/** Nº de días que hay que restar desde la fecha de [timeMillis] hasta el lunes anterior. */
private fun dayOfWeekOffset(timeMillis: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timeMillis
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    return (dow - Calendar.MONDAY + 7) % 7
}

private fun monthStartMillis(timeMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timeMillis
    cal.set(Calendar.DAY_OF_MONTH, 1)
    setStartOfDay(cal)
    return cal.timeInMillis
}

private fun addMonthsMillis(timeMillis: Long, months: Int): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timeMillis
    cal.add(Calendar.MONTH, months)
    return cal.timeInMillis
}

private const val DAY_MILLIS: Long = 24L * 60L * 60L * 1000L

