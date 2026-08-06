package com.outime.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.outime.app.presentation.viewmodel.ServiceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    serviceViewModel: ServiceViewModel,
    businessId: String,
    onBack: () -> Unit
) {
    val uiState by serviceViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("30") }
    var price by remember { mutableStateOf("0.0") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            serviceViewModel.resetState()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            scope.launch {
                snackbarHostState.showSnackbar("No se pudo crear el servicio.")
            }
            serviceViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear servicio",
                        style = MaterialTheme.typography.titleLarge,
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
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Duración (minutos)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Euro,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val duration = durationMinutes.toIntOrNull() ?: 30
                        val servicePrice = price.toDoubleOrNull() ?: 0.0
                        serviceViewModel.createService(
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
                    Text("Guardar")
                }
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