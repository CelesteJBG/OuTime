package com.outime.app.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.BusinessViewModel
import com.outime.app.presentation.viewmodel.ServiceViewModel

@Composable
fun BusinessHomeScreen(
    authViewModel: AuthViewModel,
    serviceViewModel: ServiceViewModel,
    businessViewModel: BusinessViewModel,
    onNavigateToCreateService: () -> Unit,
    onLogout: () -> Unit
) {
    val serviceUiState by serviceViewModel.uiState.collectAsState()
    val businessUiState by businessViewModel.uiState.collectAsState()

    val business = businessUiState.business
    val businessId = business?.id ?: ""

    // Carga inicial de servicios
    LaunchedEffect(businessId) {
        if (businessId.isNotEmpty()) {
            serviceViewModel.loadServices(businessId)
        }
    }

    // Recarga la lista cada vez que la pantalla vuelve a estar en RESUMED
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && businessId.isNotEmpty()) {
                serviceViewModel.loadServices(businessId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Business Home")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Negocio: ${business?.name ?: "-"}")
        Text("Descripción: ${business?.description ?: "-"}")
        Text("Categoría: ${business?.category ?: "-"}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToCreateService,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear servicio")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mis servicios",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (serviceUiState.isLoading) {
            CircularProgressIndicator()
        } else if (serviceUiState.services.isEmpty()) {
            Text("No tienes servicios creados todavía.")
        } else {
            LazyColumn {
                items(serviceUiState.services) { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = service.name,
                                fontSize = 16.sp
                            )
                            Text(text = "Duración: ${service.durationMinutes} min")
                            Text(text = "Precio: ${service.price} €")
                        }
                    }
                }
            }
        }
    }
}
