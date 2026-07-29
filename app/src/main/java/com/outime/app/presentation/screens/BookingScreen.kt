package com.outime.app.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import com.outime.app.presentation.components.TimeSlotItem
import com.outime.app.presentation.model.TimeSlot
import com.outime.app.presentation.viewmodel.AppointmentViewModel
import com.outime.app.presentation.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    clientId: String,
    businessId: String,
    businessName: String,
    serviceId: String,
    serviceName: String,
    durationMinutes: Int,
    appointmentViewModel: AppointmentViewModel,
    scheduleViewModel: ScheduleViewModel,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit
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

    // Franjas generadas para el día seleccionado
    var timeSlots by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }

    // 1) Cuando cambia la fecha seleccionada, cargar las citas del día en Firestore
    LaunchedEffect(datePickerState.selectedDateMillis, scheduleUiState.schedule) {
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

            val endOfDay = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            Log.d("BookingScreen", "Fecha cambiada → cargar citas del día")
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
                existingAppointments = appointmentUiState.dayAppointments
            )
            timeSlots = slots
            Log.d("BookingScreen", "Franjas regeneradas → ${slots.size} total, ${slots.count { it.isAvailable }} disponibles")
        }
    }

    // 3) Reaccionar al éxito de la reserva: Snackbar + recarga de citas
    LaunchedEffect(appointmentUiState.isSuccess) {
        if (appointmentUiState.isSuccess) {
            Log.d("BookingScreen", "isSuccess=true → mostrar Snackbar y recargar citas")
            scope.launch {
                snackbarHostState.showSnackbar("Cita confirmada")
            }
            appointmentViewModel.resetState()

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
            Log.e("BookingScreen", "Error en UI: $errorMsg")
            scope.launch {
                snackbarHostState.showSnackbar("Error: $errorMsg")
            }
            appointmentViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar cita") },
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
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Resumen del negocio y servicio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = businessName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = serviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Duración: $durationMinutes min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Calendario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Franjas horarias
                if (datePickerState.selectedDateMillis != null) {
                    if (scheduleUiState.isLoading || appointmentUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    } else if (timeSlots.isEmpty()) {
                        Text(
                            text = "No hay franjas disponibles para este día.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Column {
                            Text(
                                text = "Franjas disponibles",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(timeSlots) { slot ->
                                    TimeSlotItem(
                                        timeSlot = slot,
                                        isSelected = false,
                                        onClick = {
                                            Log.d("BookingScreen", "Franja pulsada: ${slot.startMillis} (disponible=${slot.isAvailable})")
                                            if (slot.isAvailable) {
                                                appointmentViewModel.createAppointment(
                                                    clientId = clientId,
                                                    businessId = businessId,
                                                    businessName = businessName,
                                                    serviceId = serviceId,
                                                    serviceName = serviceName,
                                                    dateTime = slot.startMillis
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (appointmentUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}