package com.outime.app.presentation.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.outime.app.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import com.outime.app.domain.model.UserRole

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin : () -> Unit,
    onNavigateToClientHome: () -> Unit,
    onNavigateToBusinessHome: () -> Unit

) {
    LaunchedEffect(Unit) {

        delay(1500)

        if (!authViewModel.isUserLoggedIn()) {
            onNavigateToLogin()
            return@LaunchedEffect
        }

        val user = authViewModel.getCurrentUser()

        Log.d("OUTIME", "USER FIRESTORE = $user")
        Log.d("OUTIME", "ROLE FIRESTORE = ${user?.role}")

        when (user?.role) {

            UserRole.CLIENT -> {
                onNavigateToClientHome()
            }

            UserRole.BUSINESS -> {
                onNavigateToBusinessHome()
            }

            else -> {
                onNavigateToLogin()
            }
        }
    }
}