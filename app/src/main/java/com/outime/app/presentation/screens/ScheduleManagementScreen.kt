package com.outime.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.domain.model.DaySchedule
import com.outime.app.presentation.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    // Cargar datos al entrar
    LaunchedEffect(businessId) {
        scheduleViewModel.loadSchedule(businessId)
        scheduleViewModel.loadBlockedDates(businessId)
    }

    // Cuando se carga el horario, inicializar el estado editable
    LaunchedEffect(uiState.schedule) {
        if (editableSchedule == null && uiState.schedule != null) {
            editableSchedule = uiState.schedule
        }
    }

    // Reaccionar al éxito del guardado
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            scheduleViewModel.resetState()
            scope.launch {
                snackbarHostState.showSnackbar("Disponibilidad guardada correctamente")
            }
            onNavigateBack()
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
                        text = "Gestionar disponibilidad",
                        style = MaterialTheme.typography.titleLarge,
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            val schedule = editableSchedule

            if (uiState.isLoading && schedule == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (schedule != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sección: Horario semanal
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Horario semanal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Activa los días y configura los turnos que necesites",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (uiState.isLoading && editableSchedule != null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (daySchedule.isOpen)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Switch principal: Abierto/Cerrado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = daySchedule.isOpen,
                    onCheckedChange = { isOpen ->
                        onDayChange(daySchedule.copy(isOpen = isOpen))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            if (daySchedule.isOpen) {
                // Turno de mañana
                TurnoEditor(
                    label = "Mañana",
                    start = daySchedule.morningStart,
                    end = daySchedule.morningEnd,
                    onStartChange = { onDayChange(daySchedule.copy(morningStart = it)) },
                    onEndChange = { onDayChange(daySchedule.copy(morningEnd = it)) }
                )

                // Turno de tarde
                TurnoEditor(
                    label = "Tarde",
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = start,
                onValueChange = onStartChange,
                label = { Text("Inicio") },
                placeholder = { Text("09:00") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = end,
                onValueChange = onEndChange,
                label = { Text("Fin") },
                placeholder = { Text("14:00") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
private fun BlockedDateItem(
    blockedDate: BlockedDate,
    onRemove: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
 * Normaliza un timestamp a medianoche en la zona horaria local.
 * El DatePicker de Material3 devuelve UTC millis, hay que convertirlo.
 */
private fun normalizeToMidnight(utcMillis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = utcMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}