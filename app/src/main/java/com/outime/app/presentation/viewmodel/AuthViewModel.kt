package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun currentUserId(): String?{
        return authRepository.getCurrentUserId()
    }


}