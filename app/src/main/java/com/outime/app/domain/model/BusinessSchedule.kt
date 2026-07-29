package com.outime.app.domain.model

/**
 * Horario semanal configurable del negocio.
 *
 * weeklyHours usa como clave el día de la semana según [Calendar.DAY_OF_WEEK]:
 * 1 = Domingo, 2 = Lunes, ..., 7 = Sábado.
 */
data class BusinessSchedule(
    val businessId: String = "",
    val weeklyHours: Map<Int, DaySchedule> = emptyMap()
)

/**
 * Configuración de un día de la semana.
 *
 * Soporta de 0 a 2 turnos por día (turno de mañana y/o tarde).
 * Si un turno no se utiliza, sus horas se dejan vacías ("").
 *
 * Ejemplos válidos:
 * - Solo mañana: morningStart="09:00", morningEnd="14:00", afternoon*=""
 * - Mañana y tarde: morning 09:00-14:00, afternoon 16:00-20:00
 * - Solo tarde: morning*="", afternoon 16:00-22:00
 */
data class DaySchedule(
    val isOpen: Boolean = false,
    val morningStart: String = "",
    val morningEnd: String = "",
    val afternoonStart: String = "",
    val afternoonEnd: String = ""
)