package com.outime.app.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.outime.app.presentation.viewmodel.AuthViewModel
import androidx.compose.runtime.*
import com.outime.app.domain.model.User
import kotlinx.coroutines.delay
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BusinessHomeScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit

) {
    var user by remember {
        mutableStateOf<User?>(null)
    }

    LaunchedEffect(Unit) {
        user = authViewModel.getCurrentUser()
    }
    Column (
        modifier = Modifier.padding(16.dp)
    ){

        Text("Business Home")

        Text("Nombre: ${user?.name}")

        Text("Email: ${user?.email}")

        Text("Rol: ${user?.role}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            }
        ) {
            Text("Cerrar sesión")
        }
    }


}