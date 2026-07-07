package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.outime.app.domain.repository.BusinessRepository
import com.outime.app.domain.repository.ServiceRepository

class BusinessCatalogViewModelFactory(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(BusinessCatalogViewModel::class.java)) {
            return BusinessCatalogViewModel(businessRepository, serviceRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
