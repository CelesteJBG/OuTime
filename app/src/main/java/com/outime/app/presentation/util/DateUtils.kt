package com.outime.app.presentation.util

import java.util.Calendar
import java.util.TimeZone

/**
 * Utilidades para unificar la representación de FECHAS DE CALENDARIO frente a
 * INSTANTES LOCALES, evitando el desplazamiento de un día en zonas horarias.
 *
 * Regla central:
 *  - Una FECHA DE CALENDARIO (p. ej. "16/09/2026") NO es un instante temporal: la
 *    representamos canónicamente como la **medianoche UTC** de ese día
 *    (función [utcMidnight]).
 *  - El DatePicker de Material3 entrega `DatePickerState.selectedDateMillis` ya en
 *    medianoche UTC del día elegido, por lo que [utcMidnight] sobre ese valor lo deja
 *    igual (idempotente).
 *  - La HORA de servicio/cita (p. ej. "16/09/2026 10:00") sí es un instante **local**
 *    del negocio; para los límites del día y los turnos usamos las funciones
 *    [localStartOfDay]/[localEndOfDay], que expanden la fecha a hora local sin
 *    reinterpretar el instante UTC (que desplazaría la fecha).
 *
 * Diferencia clave a la hora de comparar contra fechas bloqueadas:
 *  - Si se tiene una medianoche UTC (viene del DatePicker)  -> [utcMidnight].
 *  - Si se tiene un instante local (franja/cita)            -> [utcMidnightOfLocalDate].
 */
private val DateUtilsUtc: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * Medíanoche UTC de un epoch dado. Idempotente: si [epochMs] ya es medianoche UTC
 * (como los valores del DatePicker), devuelve el mismo valor.
 */
fun utcMidnight(epochMs: Long): Long =
    Calendar.getInstance(DateUtilsUtc).apply {
        timeInMillis = epochMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

/**
 * Día de la semana ([Calendar.DAY_OF_WEEK]; 1=Dom..7=Sáb) de la FECHA DE CALENDARIO
 * representada por una medianoche UTC. Se lee en UTC para no desplazar la fecha.
 */
fun dayOfWeekOfUtcDate(utcMid: Long): Int =
    Calendar.getInstance(DateUtilsUtc).apply {
        timeInMillis = utcMid
    }.get(Calendar.DAY_OF_WEEK)

/**
 * Medíanoche LOCAL del mismo día de calendario representado por la medianoche UTC [utcMid].
 * Se usa para los límites del día en la zona local del negocio (turnos y citas).
 */
fun localStartOfDay(utcMid: Long): Long {
    val date = Calendar.getInstance(DateUtilsUtc).apply { timeInMillis = utcMid }
    return Calendar.getInstance().apply {
        clear()
        set(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH),
            0, 0, 0
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Fin del día (23:59:59.999) LOCAL del mismo día de calendario representado por [utcMid].
 */
fun localEndOfDay(utcMid: Long): Long {
    val date = Calendar.getInstance(DateUtilsUtc).apply { timeInMillis = utcMid }
    return Calendar.getInstance().apply {
        clear()
        set(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH),
            23, 59, 59
        )
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

/**
 * Clave UTC de la fecha de calendario de un INSTANTE LOCAL ([localMs]).
 * A partir de los componentes locales (año/mes/día) construye su medianoche UTC.
 * Se usa para comparar una cita/franja local contra fechas bloqueadas.
 *
 * NO usar sobre un valor que ya represente medianoche UTC (usar [utcMidnight]):
 * reinterpretarlo aquí volvería a desplazar la fecha en zonas negativas.
 */
fun utcMidnightOfLocalDate(localMs: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMs }
    return Calendar.getInstance(DateUtilsUtc).apply {
        clear()
        set(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH),
            0, 0, 0
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}