package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.outime.app.domain.repository.AppointmentRepository
import com.outime.app.domain.repository.AuthRepository

class AppointmentViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            return AppointmentViewModel(appointmentRepository, authRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
