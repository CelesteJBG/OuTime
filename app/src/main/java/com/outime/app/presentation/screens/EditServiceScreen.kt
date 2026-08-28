package com.outime.app.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.outime.app.presentation.viewmodel.ServiceViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de edición de un servicio. Reutiliza `ServiceTextField` de
 * CreateServiceScreen (misma apariencia del formulario de creación).
 *
 * Solo se editan los datos propios del servicio (nombre, descripción,
 * duración y precio). La imagen de portada queda fuera: hoy solo existe como
 * preview local y no se persiste en Firebase, por lo que no se amplía el
 * alcance para almacenamiento de imágenes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    serviceId: String,
    businessId: String,
    serviceViewModel: ServiceViewModel,
    onBack: () -> Unit
) {
    val uiState by serviceViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("30") }
    var price by remember { mutableStateOf("0.0") }
    var seeded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Precargar los datos del servicio desde la lista ya cargada de activos.
    val service = uiState.services.firstOrNull { it.id == serviceId }
    LaunchedEffect(serviceId, service?.id) {
        val target = uiState.services.firstOrNull { it.id == serviceId }
        if (target != null && !seeded) {
            name = target.name
            description = target.description
            durationMinutes = target.durationMinutes.toString()
            price = target.price.toString()
            seeded = true
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            serviceViewModel.resetState()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { _ ->
            scope.launch {
                snackbarHostState.showSnackbar("No se pudieron guardar los cambios del servicio.")
            }
            serviceViewModel.resetState()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar servicio",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Subtítulo descriptivo (zona blanca) ──────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 4.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "Modifica los datos de tu servicio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Nombre
                ServiceTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    placeholder = "Corte de pelo",
                    leadingIcon = Icons.Default.Title,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                ServiceTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    placeholder = "Describe el servicio...",
                    leadingIcon = Icons.Default.Description
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Duración
                ServiceTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = "Duración (minutos)",
                    placeholder = "30",
                    leadingIcon = Icons.Default.Schedule,
                    singleLine = true,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Precio
                ServiceTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Precio (€)",
                    placeholder = "0.00",
                    leadingIcon = Icons.Default.Euro,
                    singleLine = true,
                    keyboardType = KeyboardType.Decimal
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Nota sobre la imagen ─────────────────────────────────────

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Consejo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Una descripción clara del servicio, te ayudará a gestionar mejor tu catálogo de citas, permitiendo reducir la tasa de cancelaciones!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Botón Guardar cambios ────────────────────────────────────
                Button(
                    onClick = {
                        val duration = durationMinutes.toIntOrNull() ?: 30
                        val servicePrice = price.toDoubleOrNull() ?: 0.0
                        serviceViewModel.updateService(
                            serviceId = serviceId,
                            businessId = businessId,
                            name = name,
                            description = description,
                            durationMinutes = duration,
                            price = servicePrice
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !uiState.isLoading && name.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

