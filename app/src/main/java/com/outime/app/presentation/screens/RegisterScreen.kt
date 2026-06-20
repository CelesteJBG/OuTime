package com.outime.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.outime.app.domain.model.UserRole
import com.outime.app.presentation.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var selectedRole by remember {
        mutableStateOf(UserRole.CLIENT)
    }

    val uiState by authViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Registro OuTime")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Rol")

        RadioButton(
            selected = selectedRole == UserRole.CLIENT,
            onClick = { selectedRole = UserRole.CLIENT }
        )

        Text("Cliente")

        RadioButton(
            selected = selectedRole == UserRole.BUSINESS,
            onClick = { selectedRole = UserRole.BUSINESS }
        )

        Text("Negocio")

        Button(
            onClick = {
                authViewModel.register(
                    name = name,
                    email = email,
                    password = password,
                    role = selectedRole
                )
            }
        ) {
            Text("Registrarse")
        }

        if (uiState.isLoading) {
            Text("Cargando...")
        }

        uiState.error?.let {
            Text("Error: $it")
        }

        if (uiState.isSuccess) {
            Text("Usuario creado correctamente")
        }
    }
}