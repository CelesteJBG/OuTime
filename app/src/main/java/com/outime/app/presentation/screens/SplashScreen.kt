package com.outime.app.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.outime.app.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import android.util.Log

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin : () -> Unit,
    onNavigateToHome: () -> Unit

) {
    LaunchedEffect(Unit) {
        delay(1500)

        if(authViewModel.isUserLoggedIn()){
            onNavigateToHome()
        }else{
            onNavigateToLogin()
        }
    }
    Text("OuTime")
}