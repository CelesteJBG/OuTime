package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.outime.app.domain.repository.ServiceRepository

class ServiceViewModelFactory(
    private val serviceRepository: ServiceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ServiceViewModel::class.java)) {
            return ServiceViewModel(serviceRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
