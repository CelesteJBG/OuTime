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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.presentation.components.TimeSlotItem
import com.outime.app.presentation.model.TimeSlot
import com.outime.app.presentation.viewmodel.AppointmentViewModel
import com.outime.app.presentation.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    clientId: String,
    businessId: String,
    businessName: String,
    serviceId: String,
    serviceName: String,
    durationMinutes: Int,
    servicePrice: Double,
    appointmentViewModel: AppointmentViewModel,
    scheduleViewModel: ScheduleViewModel,
    onNavigateBack: () -> Unit
) {
    val scheduleUiState by scheduleViewModel.uiState.collectAsState()
    val appointmentUiState by appointmentViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar horario y fechas bloqueadas del negocio al entrar
    LaunchedEffect(businessId) {
        scheduleViewModel.loadSchedule(businessId)
        scheduleViewModel.loadBlockedDates(businessId)
    }

    // Estado del calendario
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // 1. No fechas pasadas (comparar a medianoche)
                val todayMidnight = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val candidateMidnight = Calendar.getInstance().apply {
                    timeInMillis = utcTimeMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (candidateMidnight < todayMidnight) return false

                // 2. Día de la semana laborable
                val schedule = scheduleUiState.schedule ?: return true
                val dayOfWeek = Calendar.getInstance().apply {
                    timeInMillis = utcTimeMillis
                }.get(Calendar.DAY_OF_WEEK)

                val daySchedule = schedule.weeklyHours[dayOfWeek]
                if (daySchedule == null || !daySchedule.isOpen) return false

                // 3. No está en fechas bloqueadas
                val isBlocked = scheduleUiState.blockedDates.any { blocked ->
                    val blockedMidnight = Calendar.getInstance().apply {
                        timeInMillis = blocked.date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    blockedMidnight == candidateMidnight
                }
                return !isBlocked
            }
        }
    )

    // Franja horaria seleccionada por el usuario
    var selectedTimeSlot by remember { mutableStateOf<TimeSlot?>(null) }

    // Franjas generadas para el día seleccionado
    var timeSlots by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }

    // 1) Cuando cambia la fecha seleccionada, cargar las citas del día en Firestore
    LaunchedEffect(datePickerState.selectedDateMillis, scheduleUiState.schedule) {
        val dateMillis = datePickerState.selectedDateMillis
        val schedule = scheduleUiState.schedule

        // Limpiar la franja seleccionada al cambiar de fecha
        selectedTimeSlot = null

        if (dateMillis != null && schedule != null) {
            val startOfDay = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            appointmentViewModel.loadAppointmentsByBusinessAndDate(businessId, startOfDay, endOfDay)
        } else {
            timeSlots = emptyList()
        }
    }

    // 2) Cuando llegan las citas del día (o cambia el horario), regenerar las franjas
    LaunchedEffect(appointmentUiState.dayAppointments, scheduleUiState.schedule, datePickerState.selectedDateMillis) {
        val dateMillis = datePickerState.selectedDateMillis
        val schedule = scheduleUiState.schedule

        if (dateMillis != null && schedule != null) {
            val startOfDay = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val slots = scheduleViewModel.generateTimeSlots(
                schedule = schedule,
                dateMillis = startOfDay,
                durationMinutes = durationMinutes,
                existingAppointments = appointmentUiState.dayAppointments.filter {
                        // Las citas canceladas no bloquean la franja: liberan la
                        // disponibilidad para nuevas reservas (el historial se conserva).
                        it.status != AppointmentStatus.CANCELLED
                    }
            )
            timeSlots = slots
        }
    }

    // 3) Reaccionar al éxito de la reserva: Snackbar + recarga de citas
    LaunchedEffect(appointmentUiState.isSuccess) {
        if (appointmentUiState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Cita confirmada")
            }
            appointmentViewModel.resetState()
            selectedTimeSlot = null

            // Recargar las citas del día para que la franja se marque como ocupada
            val dateMillis = datePickerState.selectedDateMillis
            if (dateMillis != null) {
                val startOfDay = Calendar.getInstance().apply {
                    timeInMillis = dateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfDay = Calendar.getInstance().apply {
                    timeInMillis = dateMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                appointmentViewModel.loadAppointmentsByBusinessAndDate(businessId, startOfDay, endOfDay)
            }
        }
    }

    // 4) Reaccionar a errores
    LaunchedEffect(appointmentUiState.error) {
        appointmentUiState.error?.let { errorMsg ->
            scope.launch {
                snackbarHostState.showSnackbar("Error: $errorMsg")
            }
            appointmentViewModel.resetState()
        }
    }

    // Formateador de fechas en español para el DatePicker
    val spanishDateFormatter = remember {
        object : DatePickerFormatter {
            private val spanishLocale = Locale.forLanguageTag("es-ES")
            private val monthYearFormat = SimpleDateFormat("MMMM yyyy", spanishLocale)
            private val dateDayMonthFormat = SimpleDateFormat("EEE, d MMM", spanishLocale)

            override fun formatDate(dateMillis: Long?, locale: Locale, forAccessibility: Boolean): String {
                return dateMillis?.let {
                    val formatted = dateDayMonthFormat.format(Date(it))
                    formatted.replaceFirstChar { c -> c.uppercase() }
                } ?: ""
            }

            override fun formatMonthYear(monthMillis: Long?, locale: Locale): String {
                return monthMillis?.let {
                    val formatted = monthYearFormat.format(Date(it))
                    formatted.replaceFirstChar { c -> c.uppercase() }
                } ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reservar cita",
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Resumen del negocio y servicio (tarjeta primary) ────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Nombre del negocio
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = businessName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        // Nombre del servicio
                        Text(
                            text = serviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        // Duración
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Duración: $durationMinutes min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // ── Sección: Selecciona una fecha ───────────────────────────
                Text(
                    text = "Selecciona una fecha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Calendario con colores del Design System
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(0.9f),
                        dateFormatter = spanishDateFormatter,
                        title = {
                            Text(
                                text = "Selecciona desde el calendario o ingresa directamente una fecha",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            navigationContentColor = MaterialTheme.colorScheme.onSurface,
                            yearContentColor = MaterialTheme.colorScheme.onSurface,
                            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            dividerColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // ── Sección: Horarios disponibles ────────────────────────────
                if (datePickerState.selectedDateMillis != null) {
                    Text(
                        text = "Horarios disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (scheduleUiState.isLoading || appointmentUiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (timeSlots.isEmpty()) {
                        Text(
                            text = "No hay franjas disponibles para este día.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Grid de franjas horarias (no-lazy para funcionar dentro de verticalScroll)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            timeSlots.chunked(3).forEach { rowSlots ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSlots.forEach { slot ->
                                        TimeSlotItem(
                                            timeSlot = slot,
                                            isSelected = selectedTimeSlot?.startMillis == slot.startMillis,
                                            onClick = {
                                                if (slot.isAvailable) {
                                                    selectedTimeSlot = if (selectedTimeSlot?.startMillis == slot.startMillis) null else slot
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Rellenar espacio si la fila tiene menos de 3 elementos
                                    repeat(3 - rowSlots.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // ── CTA: Confirmar cita ──────────────────────────────────────
                Button(
                    onClick = {
                        val slot = selectedTimeSlot ?: return@Button
                        appointmentViewModel.createAppointment(
                            clientId = clientId,
                            businessId = businessId,
                            businessName = businessName,
                            serviceId = serviceId,
                            serviceName = serviceName,
                            dateTime = slot.startMillis,
                            servicePrice = servicePrice
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = selectedTimeSlot != null,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (selectedTimeSlot != null) "Confirmar cita" else "Selecciona un horario",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Overlay de carga al crear la cita
            if (appointmentUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}