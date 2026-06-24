package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.outime.app.domain.repository.AppointmentRepository

class AppointmentViewModelFactory(
    private val appointmentRepository: AppointmentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            return AppointmentViewModel(appointmentRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
