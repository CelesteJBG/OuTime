package com.outime.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.domain.model.DaySchedule
import com.outime.app.presentation.util.utcMidnight
import com.outime.app.presentation.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementScreen(
    businessId: String,
    scheduleViewModel: ScheduleViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by scheduleViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estado editable local del horario (se inicializa cuando se carga del ViewModel)
    var editableSchedule by remember { mutableStateOf<BusinessSchedule?>(null) }

    // Datos en tiempo real (listeners de Firestore): horario y fechas bloqueadas se
    // actualizan solos al entrar y cuando cambian, sin necesidad de re-entrar.
    LaunchedEffect(businessId) {
        scheduleViewModel.observeSchedule(businessId)
        scheduleViewModel.observeBlockedDates(businessId)
    }

    // Cuando se carga el horario, inicializar el estado editable
    LaunchedEffect(uiState.schedule) {
        if (editableSchedule == null && uiState.schedule != null) {
            editableSchedule = uiState.schedule
        }
    }

    // Reaccionar al éxito del guardado: permanecer en la pantalla y mostrar confirmación.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            scheduleViewModel.resetState()
            scope.launch {
                snackbarHostState.showSnackbar("Horario actualizado correctamente")
            }
        }
    }

    // Reaccionar a errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            scope.launch {
                snackbarHostState.showSnackbar("Error: $errorMsg")
            }
            scheduleViewModel.resetState()
        }
    }

    // Diálogo para añadir fecha bloqueada
    var showBlockedDatePicker by remember { mutableStateOf(false) }
    var blockedReason by remember { mutableStateOf("") }
    val blockedDatePickerState = rememberDatePickerState()

    if (showBlockedDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showBlockedDatePicker = false
                blockedReason = ""
            },
            confirmButton = {
                TextButton(onClick = {
                    blockedDatePickerState.selectedDateMillis?.let { dateMillis ->
                        // Normalizar a medianoche zona local
                        val normalized = normalizeToMidnight(dateMillis)
                        scheduleViewModel.addBlockedDate(
                            businessId = businessId,
                            dateMillis = normalized,
                            reason = blockedReason.ifBlank { "Día no disponible" }
                        )
                    }
                    showBlockedDatePicker = false
                    blockedReason = ""
                }) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBlockedDatePicker = false
                    blockedReason = ""
                }) {
                    Text("Cancelar")
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePicker(state = blockedDatePickerState)
                OutlinedTextField(
                    value = blockedReason,
                    onValueChange = { blockedReason = it },
                    label = { Text("Motivo (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Horario semanal",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // ── Zona blanca: subtítulo que extiende el fondo del TopAppBar ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "Activa los días y configura los turnos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // ── Padding entre la zona blanca y el fondo marfil ──
            Spacer(modifier = Modifier.height(20.dp))

            val schedule = editableSchedule

            if (uiState.isLoading && schedule == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (schedule != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val dayNames = listOf(
                        Calendar.MONDAY to "Lunes",
                        Calendar.TUESDAY to "Martes",
                        Calendar.WEDNESDAY to "Miércoles",
                        Calendar.THURSDAY to "Jueves",
                        Calendar.FRIDAY to "Viernes",
                        Calendar.SATURDAY to "Sábado",
                        Calendar.SUNDAY to "Domingo"
                    )

                    items(dayNames) { (dayOfWeek, dayName) ->
                        DayScheduleCard(
                            dayName = dayName,
                            daySchedule = schedule.weeklyHours[dayOfWeek] ?: DaySchedule(),
                            onDayChange = { newDay ->
                                editableSchedule = schedule.copy(
                                    weeklyHours = schedule.weeklyHours + (dayOfWeek to newDay)
                                )
                            }
                        )
                    }

                    // Sección: Fechas bloqueadas
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fechas bloqueadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vacaciones, festivos o días no disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (uiState.blockedDates.isEmpty()) {
                        item {
                            Text(
                                text = "No hay fechas bloqueadas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.blockedDates) { blockedDate ->
                            BlockedDateItem(
                                blockedDate = blockedDate,
                                onRemove = {
                                    scheduleViewModel.removeBlockedDate(
                                        blockedDateId = blockedDate.id,
                                        businessId = businessId
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showBlockedDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Añadir fecha bloqueada")
                        }
                    }

                    // Botón Guardar
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                editableSchedule?.let {
                                    scheduleViewModel.saveSchedule(it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            enabled = !uiState.isLoading
                        ) {
                            Text("Guardar disponibilidad")
                        }
                    }
                }
            }

            if (uiState.isLoading && editableSchedule != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DayScheduleCard(
    dayName: String,
    daySchedule: DaySchedule,
    onDayChange: (DaySchedule) -> Unit
) {
    val isOpen = daySchedule.isOpen

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.background
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isOpen)
                MaterialTheme.colorScheme.outlineVariant
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fila superior: nombre del día + toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOpen)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isOpen,
                    onCheckedChange = { newIsOpen ->
                        onDayChange(daySchedule.copy(isOpen = newIsOpen))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Solo se muestran las franjas si el día está activo
            if (isOpen) {
                // Separador sutil entre cabecera del día y sus horarios
                HorizontalDivider()

                // Turno de mañana
                TurnoEditor(
                    label = "MAÑANA",
                    start = daySchedule.morningStart,
                    end = daySchedule.morningEnd,
                    onStartChange = { onDayChange(daySchedule.copy(morningStart = it)) },
                    onEndChange = { onDayChange(daySchedule.copy(morningEnd = it)) }
                )

                // Separación extra entre MAÑANA y TARDE
                Spacer(modifier = Modifier.height(4.dp))

                // Turno de tarde
                TurnoEditor(
                    label = "TARDE",
                    start = daySchedule.afternoonStart,
                    end = daySchedule.afternoonEnd,
                    onStartChange = { onDayChange(daySchedule.copy(afternoonStart = it)) },
                    onEndChange = { onDayChange(daySchedule.copy(afternoonEnd = it)) }
                )
            }
        }
    }
}

@Composable
private fun TurnoEditor(
    label: String,
    start: String,
    end: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Etiqueta de franja — separador visual entre mañana y tarde
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
        )

        // Etiquetas Inicio / Fin
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Inicio",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Fin",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        // Campos de hora
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeField(
                value = start,
                onValueChange = onStartChange,
                placeholder = "09:00",
                modifier = Modifier.weight(1f)
            )
            TimeField(
                value = end,
                onValueChange = onEndChange,
                placeholder = "14:00",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberTimePickerState(
        initialHour = parseHours(value),
        initialMinute = parseMinutes(value),
        is24Hour = true
    )

    Box(modifier = modifier) {
        // Campo visualmente idéntico al original, solo lectura: la interacción
        // principal es el selector visual mediante el TimePicker.
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = "Hora",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background
            )
        )

        // Superficie clicable encima del campo que abre el selector de hora.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
                .semantics { contentDescription = "Seleccionar hora" }
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            pickerState.hour,
                            pickerState.minute
                        )
                    )
                    showPicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = pickerState)
                }
            }
        )
    }
}

/** Extrae la hora (0-23) de un valor "HH:mm"; por defecto 9 si no es válido. */
private fun parseHours(time: String): Int =
    time.substringBefore(':', "").toIntOrNull()?.coerceIn(0, 23) ?: 9

/** Extrae los minutos (0-59) de un valor "HH:mm"; por defecto 0 si no es válido. */
private fun parseMinutes(time: String): Int =
    time.substringAfter(':', "").toIntOrNull()?.coerceIn(0, 59) ?: 0

@Composable
private fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun BlockedDateItem(
    blockedDate: BlockedDate,
    onRemove: () -> Unit
) {
    val dateFormatter = remember {
        // Se formatea en UTC para mostrar exactamente la fecha de calendario almacenada
        // (el valor se guarda como medianoche UTC), sin desplazarla por la zona horaria.
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateFormatter.format(blockedDate.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (blockedDate.reason.isNotBlank()) {
                    Text(
                        text = blockedDate.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Normaliza un timestamp a la medianoche UTC de la fecha de calendario que representa.
 * El DatePicker de Material3 ya devuelve los millis UTC del día elegido, así que esta
 * función es idempotente sobre ese valor y evita desplazarlo a la zona horaria local.
 */
private fun normalizeToMidnight(utcMillis: Long): Long =
    utcMidnight(utcMillis)
