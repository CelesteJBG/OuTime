package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.outime.app.domain.model.User
import com.outime.app.domain.model.UserRole
import com.outime.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ) {
        viewModelScope.launch {

            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.register(
                name = name,
                email = email,
                password = password,
                role = role
            )

            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(
                        isSuccess = true,
                        currentUserId = authRepository.getCurrentUserId()
                    )
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        error = error.message
                    )
                }
            )
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.login(
                email = email,
                password = password
            )

            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(
                        isSuccess = true,
                        currentUserId = authRepository.getCurrentUserId()
                    )
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        error = error.message
                    )
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    fun isUserLoggedIn(): Boolean{
        return authRepository.getCurrentUserId() != null
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {

            _uiState.value = AuthUiState(isLoading = true)

            val result = authRepository.sendPasswordResetEmail(email)

            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(isResetSent = true)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        error = mapPasswordResetError(error)
                    )
                }
            )
        }
    }

    /**
     * Convierte un error de Firebase en un mensaje seguro y genérico.
     * Nunca expone detalles técnicos ni información sobre la existencia de cuentas.
     */
    private fun mapPasswordResetError(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidCredentialsException -> "Introduce un correo electrónico válido."
        else -> "No se pudo enviar el correo. Inténtalo de nuevo."
    }

    fun currentUserId(): String?{
        return authRepository.getCurrentUserId()
    }

    suspend fun getCurrentUser(): User?{
        return authRepository.getCurrentUser()
    }

    fun updateUser(
        name: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            val uid = authRepository.getCurrentUserId()
                ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo identificar la sesión. Vuelve a iniciar sesión."
                    )
                    return@launch
                }

            val current = authRepository.getCurrentUser()

            val user = User(
                id = uid,
                name = name,
                email = current?.email ?: "",
                role = current?.role ?: UserRole.CLIENT,
                createdAt = current?.createdAt ?: System.currentTimeMillis()
            )

            val result = authRepository.updateUser(user)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

}