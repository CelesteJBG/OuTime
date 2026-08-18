package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.outime.app.domain.repository.AppointmentRepository
import com.outime.app.domain.repository.ServiceRepository

class StatisticsViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(appointmentRepository, serviceRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
