package com.outime.app.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outime.app.presentation.model.TimeSlot
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Botón que representa una franja horaria en la cuadrícula de reserva.
 *
 * - Disponible: fondo secondaryContainer, clickable.
 * - Ocupada: fondo errorContainer, no clickable.
 * - Seleccionada: fondo primary.
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
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when {
        !timeSlot.isAvailable -> MaterialTheme.colorScheme.onErrorContainer
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Button(
        onClick = onClick,
        enabled = timeSlot.isAvailable,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp),
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