package com.outime.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.outime.app.domain.model.Business
import com.outime.app.domain.model.User
import com.outime.app.presentation.components.AccountEmailInfo
import com.outime.app.presentation.components.BusinessCategorySelector
import com.outime.app.presentation.components.ProfileEditField
import com.outime.app.presentation.components.ProfileEditIcons
import com.outime.app.presentation.viewmodel.AuthViewModel
import com.outime.app.presentation.viewmodel.BusinessViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBusinessProfileScreen(
    businessViewModel: BusinessViewModel,
    authViewModel: AuthViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by businessViewModel.uiState.collectAsState()

    // Negocio actual ya cargado en el ViewModel (se observa reactivamente)
    val currentBusiness = uiState.business

    // Dueño del negocio: se carga para mostrar el email en modo solo lectura
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        currentUser = authViewModel.getCurrentUser()
    }

    var name by remember(currentBusiness) { mutableStateOf(currentBusiness?.name ?: "") }
    var description by remember(currentBusiness) { mutableStateOf(currentBusiness?.description ?: "") }
    var category by remember(currentBusiness) { mutableStateOf(currentBusiness?.category ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            businessViewModel.resetState()
            onSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar("No se pudo guardar: $msg")
            }
            businessViewModel.resetState()
        }
    }

    val nameError = remember(name) {
        if (name.isBlank()) "El nombre del negocio no puede estar vacío" else null
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar negocio",
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileEditField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del negocio",
                    icon = ProfileEditIcons.BusinessName,
                    isError = nameError != null,
                    supportingText = nameError
                )

                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BusinessCategorySelector(
                    selected = category,
                    onSelected = { category = it }
                )

                ProfileEditField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    icon = ProfileEditIcons.Description,
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                AccountEmailInfo(
                    label = "Correo electrónico",
                    email = currentUser?.email.orEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val business = currentBusiness?.copy(
                            name = name.trim(),
                            description = description.trim(),
                            category = category.trim()
                        )
                        if (business != null) {
                            businessViewModel.updateBusiness(business)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !uiState.isLoading && name.isNotBlank() && currentBusiness != null
                ) {
                    Text("Guardar cambios")
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