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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.presentation.components.getStatusConfig
import com.outime.app.presentation.components.groupAppointmentsByDate
import com.outime.app.presentation.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientAppointmentsScreen(
    clientId: String,
    appointmentViewModel: AppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    val appointmentUiState by appointmentViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf<AppointmentStatus?>(null) }

    // Cargar citas del cliente al entrar
    LaunchedEffect(clientId) {
        if (clientId.isNotEmpty()) {
            appointmentViewModel.loadAppointmentsByClient(clientId)
        }
    }

    // Reaccionar al éxito de una acción (cancelar)
    LaunchedEffect(appointmentUiState.isSuccess) {
        if (appointmentUiState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Reserva cancelada.")
            }
            appointmentViewModel.resetState()
            // Recargar la lista
            if (clientId.isNotEmpty()) {
                appointmentViewModel.loadAppointmentsByClient(clientId)
            }
        }
    }

    // Reaccionar a errores
    LaunchedEffect(appointmentUiState.error) {
        appointmentUiState.error?.let { errorMsg ->
            scope.launch {
                snackbarHostState.showSnackbar("Error: $errorMsg")
            }
            appointmentViewModel.resetState()
        }
    }

    // Filtrar y agrupar citas
    val allAppointments = appointmentUiState.appointments
    val filteredAppointments = if (selectedFilter == null) {
        allAppointments
    } else {
        allAppointments.filter { it.status == selectedFilter }
    }
    val groupedAppointments = groupAppointmentsByDate(filteredAppointments)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis reservas",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Filtros
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("Todas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AppointmentStatus.CONFIRMED,
                        onClick = { selectedFilter = AppointmentStatus.CONFIRMED },
                        label = { Text("Confirmadas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AppointmentStatus.COMPLETED,
                        onClick = { selectedFilter = AppointmentStatus.COMPLETED },
                        label = { Text("Completadas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == AppointmentStatus.CANCELLED,
                        onClick = { selectedFilter = AppointmentStatus.CANCELLED },
                        label = { Text("Canceladas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Contenido principal
            if (appointmentUiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (appointmentUiState.error != null && allAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Error al cargar las reservas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                if (clientId.isNotEmpty()) {
                                    appointmentViewModel.loadAppointmentsByClient(clientId)
                                }
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (filteredAppointments.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Todavía no tienes reservas.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedFilter != null) {
                            Text(
                                text = "No hay reservas con este filtro.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Lista de citas agrupadas por fecha
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 24.dp
                    )
                ) {
                    groupedAppointments.forEach { (dateLabel, appointmentsForDate) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(appointmentsForDate) { appointment ->
                            ClientAppointmentCard(
                                appointment = appointment,
                                onCancel = {
                                    appointmentViewModel.updateAppointmentStatus(
                                        appointment.id,
                                        AppointmentStatus.CANCELLED
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientAppointmentCard(
    appointment: Appointment,
    onCancel: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val statusConfig = getStatusConfig(appointment.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila superior: hora + servicio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormat.format(appointment.dateTime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = appointment.serviceName.ifBlank { "Servicio" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
            }

            // Negocio con icono
            if (appointment.businessName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = appointment.businessName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Estado (chip visual) + acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = statusConfig.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = statusConfig.color
                    )
                    Text(
                        text = statusConfig.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusConfig.color
                    )
                }

                // Acción: solo cancelar si está confirmada
                if (appointment.status == AppointmentStatus.CONFIRMED) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}