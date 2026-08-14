package com.outime.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.outime.app.R
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Isotipo / logo OuTime ──
        Image(
            painter = painterResource(R.drawable.outime_logo_vertical),
            contentDescription = "OuTime",
            modifier = Modifier.size(220.dp),
            contentScale = ContentScale.Fit
        )

        //Spacer(modifier = Modifier.height(6.dp))

        // ── Subtítulo ──
        Text(
            text = "Reserva tu cita en segundos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ── Indicador de carga ──
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
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