package com.outime.app.presentation.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.AppointmentStatus
import com.outime.app.presentation.components.getStatusConfig
import com.outime.app.presentation.util.ScanOutcome
import com.outime.app.presentation.util.evaluateScannedAppointment
import com.outime.app.presentation.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pantalla de negocio para escanear el QR de una cita (Google Code Scanner).
 *
 * Flujo: escanear -> appointmentId -> consulta Firestore -> validar (solo CONFIRMED
 * y del mismo negocio) -> mostrar datos reales -> [Marcar como completada].
 *
 * Escanear no modifica ni elimina nada: solo la acción explícita de completar
 * cambia el estado a COMPLETED reutilizando AppointmentViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessQrScanScreen(
    businessId: String,
    appointmentViewModel: AppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by appointmentViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val scanner: GmsBarcodeScanner = remember(context) {
        GmsBarcodeScanning.getClient(context)
    }

    // appointmentId escaneado (null = aún sin escanear / listos para otro escaneo)
    var scannedId by remember { mutableStateOf<String?>(null) }

    fun startScanning() {
        if (scannedId != null) return

        val task = scanner.startScan()
        task.addOnSuccessListener { barcode ->
            val value = barcode.rawValue?.trim().orEmpty()
            if (value.isBlank()) {
                scannedId = null
                scope.launch {
                    snackbarHostState.showSnackbar("El código QR no corresponde a una cita válida.")
                }
            } else {
                scannedId = value
                appointmentViewModel.loadScannedAppointment(value)
            }
        }
        task.addOnCanceledListener { /* El usuario cerró el escáner: no hacemos nada. */ }
        task.addOnFailureListener { error ->
            val isCancel = error is ApiException &&
                error.statusCode == CommonStatusCodes.CANCELED
            if (!isCancel) {
                scannedId = null
                scope.launch {
                    snackbarHostState.showSnackbar("No se pudo escanear. Revisa la cámara/permisos.")
                }
            }
        }
    }

    // Feedback al completar una cita: mensaje + refresco visual del estado.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Cita marcada como completada correctamente")
            }
            appointmentViewModel.resetState()
            scannedId?.let { appointmentViewModel.loadScannedAppointment(it) }
        }
    }

    // Errores de red/Firestore
    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar("Error: $msg") }
            appointmentViewModel.resetState()
        }
    }

    val scannedAppointment = uiState.selectedAppointment
    val isLoading = uiState.isLoading
    val outcome: ScanOutcome? = if (scannedId != null && !isLoading) {
        evaluateScannedAppointment(scannedAppointment, businessId)
    } else {
        null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escanear cita",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Estado inicial: aún no se ha escaneado
                scannedId == null -> ScanStartView(onScanClick = ::startScanning)

                // Cargando la cita en Firestore
                isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

                // Resultado del escaneo
                outcome != null -> ScanResultView(
                    outcome = outcome,
                    appointment = scannedAppointment,
                    clientName = uiState.scannedClientName,
                    onMarkCompleted = {
                        val appointment = scannedAppointment ?: return@ScanResultView
                        appointmentViewModel.updateAppointmentStatus(
                            appointment.id,
                            AppointmentStatus.COMPLETED
                        )
                    },
                    onScanAgain = {
                        scannedId = null
                        appointmentViewModel.resetState()
                        startScanning()
                    }
                )
            }
        }
    }
}

@Composable
private fun ScanStartView(onScanClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp),
            content = {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        )
        Text(
            text = "Escanea el código QR del cliente para validar su cita.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onScanClick,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear QR")
        }
    }
}

@Composable
private fun ScanResultView(
    outcome: ScanOutcome,
    appointment: Appointment?,
    clientName: String?,
    onMarkCompleted: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (outcome) {
            ScanOutcome.VALID -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Cita identificada",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (appointment != null) {
                    AppointmentScanCard(appointment = appointment, clientName = clientName)

                    Button(
                        onClick = onMarkCompleted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marcar como completada")
                    }
                }
            }

            ScanOutcome.NOT_AN_APPOINTMENT -> ErrorScanCard(
                message = "El código QR no corresponde a una cita válida."
            )
            ScanOutcome.OTHER_BUSINESS -> ErrorScanCard(
                message = "Esta cita no pertenece a tu negocio."
            )
            ScanOutcome.CANCELLED -> ErrorScanCard(
                message = "Esta cita está cancelada y no puede procesarse."
            )
            ScanOutcome.ALREADY_COMPLETED -> ErrorScanCard(
                message = "La cita ya está completada."
            )
            ScanOutcome.NOT_CONFIRMED -> ErrorScanCard(
                message = "La cita está pendiente y no puede completarse."
            )
        }

        OutlinedButton(
            onClick = onScanAgain,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Escanear otro QR")
        }
    }
}

@Composable
private fun ErrorScanCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Muestra los datos reales de la cita obtenidos de Firestore. */
@Composable
private fun AppointmentScanCard(appointment: Appointment, clientName: String?) {
    val statusConfig = getStatusConfig(appointment.status)
    val dateTimeFormat = remember {
        SimpleDateFormat("d 'de' MMMM 'de' yyyy · HH:mm", Locale.forLanguageTag("es-ES"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = clientName ?: appointment.clientId,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            ScanRow(Icons.Default.Store, appointment.serviceName.ifBlank { "Servicio" })
            ScanRow(Icons.Default.Schedule, dateTimeFormat.format(appointment.dateTime))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusConfig.color.copy(alpha = 0.12f),
                    content = {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = statusConfig.icon,
                                contentDescription = null,
                                tint = statusConfig.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = statusConfig.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusConfig.color
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ScanRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}