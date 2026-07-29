package com.outime.app.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.outime.app.presentation.model.TimeSlot
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Botón que representa una franja horaria en la cuadrícula de reserva.
 *
 * - 🟢 Disponible: fondo verde claro, clickable.
 * - 🔴 Ocupada: fondo rojo claro, no clickable.
 * - Seleccionada: fondo verde intenso.
 */
@Composable
fun TimeSlotItem(
    timeSlot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val containerColor = when {
        !timeSlot.isAvailable -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        !timeSlot.isAvailable -> MaterialTheme.colorScheme.onErrorContainer
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Button(
        onClick = onClick,
        enabled = timeSlot.isAvailable,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    ) {
        Text(
            text = timeFormat.format(timeSlot.startMillis),
            fontWeight = FontWeight.Medium
        )
    }
}