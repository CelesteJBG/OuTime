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
import androidx.compose.runtime.key
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
import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.presentation.components.TimeSlotItem
import com.outime.app.presentation.model.TimeSlot
import com.outime.app.presentation.util.dayOfWeekOfUtcDate
import com.outime.app.presentation.util.localEndOfDay
import com.outime.app.presentation.util.localStartOfDay
import com.outime.app.presentation.util.utcMidnight
import com.outime.app.presentation.util.utcMidnightOfLocalDate
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

    // Horario y fechas bloqueadas en tiempo real (listeners de Firestore). Se activan una
    // única vez por entrada: el ViewModel mantiene la suscripción y el estado se actualiza
    // automáticamente cuando el negocio cambia horarios o bloquea/desbloquea fechas.
    LaunchedEffect(businessId) {
        scheduleViewModel.observeSchedule(businessId)
        scheduleViewModel.observeBlockedDates(businessId)
    }

    // Huella de la disponibilidad (horario semanal + fechas bloqueadas). Al cambiar
    // (p. ej. el negocio acaba de bloquear una fecha), forzamos que el DatePicker se
    // reconstruya y recalcule qué días se pueden seleccionar, reflejando al instante
    // las fechas bloqueadas y los días apagados.
    val availabilitySignature = remember(scheduleUiState.schedule, scheduleUiState.blockedDates) {
        val weekly = scheduleUiState.schedule?.weeklyHours
            ?.entries
            ?.map { (k, v) ->
                "$k:${v.isOpen}:${v.morningStart}:${v.morningEnd}:${v.afternoonStart}:${v.afternoonEnd}"
            }
            ?.sorted()
            ?.joinToString("|")
            .orEmpty()
        val blocked = scheduleUiState.blockedDates
            .map { it.date }
            .sorted()
            .joinToString(",")
        "$weekly#$blocked"
    }

    // Estado del calendario
    val datePickerState = key(availabilitySignature) {
        rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // 1. No fechas pasadas (comparación de fecha de calendario en UTC)
                    val todayUtcMid = utcMidnight(System.currentTimeMillis())
                    val candidateUtcMid = utcMidnight(utcTimeMillis)
                    if (candidateUtcMid < todayUtcMid) return false

                    // 2. Día de la semana laborable (semántica de FECHA: se lee en UTC para
                    //    no desplazar la fecha elegida a la zona horaria local)
                    val schedule = scheduleUiState.schedule ?: return true
                    val dayOfWeek = dayOfWeekOfUtcDate(candidateUtcMid)

                    val daySchedule = schedule.weeklyHours[dayOfWeek]
                    if (daySchedule == null || !daySchedule.isOpen) return false

                    // 3. No está en fechas bloqueadas (misma clave UTC de fecha)
                    val isBlocked = scheduleUiState.blockedDates.any { blocked ->
                        utcMidnight(blocked.date) == candidateUtcMid
                    }
                    return !isBlocked
                }
            }
        )
    }

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
            val startOfDay = localStartOfDay(dateMillis)
            val endOfDay = localEndOfDay(dateMillis)

            appointmentViewModel.loadAppointmentsByBusinessAndDate(businessId, startOfDay, endOfDay)
        } else {
            timeSlots = emptyList()
        }
    }

    // 2) Cuando llegan las citas del día (o cambia el horario), regenerar las franjas
    LaunchedEffect(appointmentUiState.dayAppointments, scheduleUiState.schedule, scheduleUiState.blockedDates, datePickerState.selectedDateMillis) {
        val dateMillis = datePickerState.selectedDateMillis
        val schedule = scheduleUiState.schedule

        if (dateMillis != null && schedule != null) {
            val startOfDay = localStartOfDay(dateMillis)

            val slots = scheduleViewModel.generateTimeSlots(
                schedule = schedule,
                dateMillis = startOfDay,
                durationMinutes = durationMinutes,
                existingAppointments = appointmentUiState.dayAppointments.filter {
                        // Las citas canceladas no bloquean la franja: liberan la
                        // disponibilidad para nuevas reservas (el historial se conserva).
                        it.status != AppointmentStatus.CANCELLED
                    },
                blockedDates = scheduleUiState.blockedDates
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
                val startOfDay = localStartOfDay(dateMillis)
                val endOfDay = localEndOfDay(dateMillis)

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

                        // Refuerzo anti-carrera: en el instante de confirmar se revalida que
                        // la fecha siga disponible (no bloqueada ni con el día apagado), aunque
                        // la data en pantalla haya quedado desactualizada por un bloqueo previo.
                        val stillAvailable = isBookingDateStillAvailable(
                            dateMillis = slot.startMillis,
                            schedule = scheduleUiState.schedule,
                            blockedDates = scheduleUiState.blockedDates
                        )
                        if (!stillAvailable) {
                            selectedTimeSlot = null
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Ese día ya no está disponible. Elige otra fecha."
                                )
                            }
                            return@Button
                        }

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
/**
 * Revalida en el momento de confirmar la cita que la fecha siga siendo reservable
 * (no es una fecha bloqueada por el negocio ni un día apagado en el horario semanal).
 *
 * Es una mitigación del hueco de concurrencia (race condition) descrito en
 * AppointmentRepositoryImpl.createAppointment. Nótese que el calendario de Material3
 * puede mostrar un día como seleccionable aunque la data haya cambiado mientras se
 * visualizaba; esta comprobación evita reservar sobre una fecha que quedó bloqueada.
 */
private fun isBookingDateStillAvailable(
    dateMillis: Long,
    schedule: BusinessSchedule?,
    blockedDates: List<BlockedDate>
): Boolean {
    // [dateMillis] es el instante local del inicio de la franja. Obtenemos la clave UTC
    // de su día de calendario (equivalente a la representación canónica de las fechas
    // bloqueadas).
    val dateKey = utcMidnightOfLocalDate(dateMillis)

    // Fecha bloqueada por el negocio (misma representación canónica UTC)
    val isBlocked = blockedDates.any { blocked ->
        utcMidnight(blocked.date) == dateKey
    }
    if (isBlocked) return false

    // Día apagado en el horario semanal (la franja es un instante local -> weekday local)
    if (schedule != null) {
        val dayOfWeek = Calendar.getInstance().apply { timeInMillis = dateMillis }
            .get(Calendar.DAY_OF_WEEK)
        val daySchedule = schedule.weeklyHours[dayOfWeek]
        if (daySchedule == null || !daySchedule.isOpen) return false
    }

    return true
}