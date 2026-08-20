package com.outime.app.domain.model

/**
 * Fecha bloqueada por el negocio (vacaciones, festivos, asuntos personales...).
 *
 * [date] representa una FECHA DE CALENDARIO (no un instante): se almacena como
 * epoch ms a la medianoche UTC de ese día, y no en zona horaria local.
 * Ver `com.outime.app.presentation.util.DateUtils`.
 *
 * NOTA (datos antiguos): las fechas bloqueadas creadas durante pruebas previas
 * podrían haberse guardado con un valor desplazado un día al mezclar UTC/local.
 * En desarrollo/testing conviene borrarlas y recrearlas tras este cambio.
 */
data class BlockedDate(
    val id: String = "",
    val businessId: String = "",
    val date: Long = 0L,
    val reason: String = ""
)