package com.outime.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outime.app.domain.model.UserRole
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.BusinessViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    businessViewModel: BusinessViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToClientHome: () -> Unit,
    onNavigateToBusinessHome: () -> Unit,
    onNavigateToCreateBusiness: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OuTime",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Reserva tu cita en segundos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }

    LaunchedEffect(Unit) {

        delay(1500)

        if (!authViewModel.isUserLoggedIn()) {
            onNavigateToLogin()
            return@LaunchedEffect
        }

        val user = authViewModel.getCurrentUser()

        when (user?.role) {

            UserRole.CLIENT -> {
                onNavigateToClientHome()
            }

            UserRole.BUSINESS -> {
                val ownerId = authViewModel.currentUserId() ?: ""
                val business = businessViewModel.getBusinessByOwnerId(ownerId)
                if (business != null) {
                    onNavigateToBusinessHome()
                } else {
                    onNavigateToCreateBusiness()
                }
            }

            else -> {
                onNavigateToLogin()
            }
        }
    }
}