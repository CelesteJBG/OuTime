package com.outime.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Configuración visual de un estado de cita (label, color, icono).
 * Compartido entre BusinessAppointmentsScreen y ClientAppointmentsScreen.
 */
data class StatusConfig(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

/**
 * Devuelve la configuración visual para un estado de cita.
 */
@Composable
fun getStatusConfig(status: AppointmentStatus): StatusConfig {
    return when (status) {
        AppointmentStatus.CONFIRMED -> StatusConfig(
            label = "Confirmada",
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Check
        )
        AppointmentStatus.COMPLETED -> StatusConfig(
            label = "Completada",
            color = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Default.Check
        )
        AppointmentStatus.CANCELLED -> StatusConfig(
            label = "Cancelada",
            color = MaterialTheme.colorScheme.error,
            icon = Icons.Default.Close
        )
        AppointmentStatus.PENDING -> StatusConfig(
            label = "Pendiente",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.EventBusy
        )
    }
}

/**
 * Agrupa las citas por fecha con etiquetas inteligentes:
 * "Hoy", "Mañana", o "31 de julio".
 *
 * Compartido entre BusinessAppointmentsScreen y ClientAppointmentsScreen.
 */
fun groupAppointmentsByDate(
    appointments: List<Appointment>
): Map<String, List<Appointment>> {
    val sorted = appointments.sortedBy { it.dateTime }
    val grouped = LinkedHashMap<String, MutableList<Appointment>>()

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrow = (today.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, 1)
    }

    val dayFormat = SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-ES"))

    for (appointment in sorted) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = appointment.dateTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val label = when {
            cal == today -> "Hoy"
            cal == tomorrow -> "Mañana"
            else -> dayFormat.format(appointment.dateTime)
        }

        grouped.getOrPut(label) { mutableListOf() }.add(appointment)
    }

    return grouped
}