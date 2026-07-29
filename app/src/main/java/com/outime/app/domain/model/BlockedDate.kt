package com.outime.app.domain.model

/**
 * Fecha bloqueada por el negocio (vacaciones, festivos, asuntos personales...).
 *
 * [date] se almacena como epoch ms a medianoche (zona horaria local).
 */
data class BlockedDate(
    val id: String = "",
    val businessId: String = "",
    val date: Long = 0L,
    val reason: String = ""
)