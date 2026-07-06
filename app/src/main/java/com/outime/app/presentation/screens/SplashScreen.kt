package com.outime.app.presentation.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    Text("")

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
