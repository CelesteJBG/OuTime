package com.outime.app.domain.repository

import com.outime.app.domain.model.Business
import kotlinx.coroutines.flow.Flow

interface BusinessRepository {

    suspend fun createBusiness(business: Business): Result<Business>

    suspend fun getBusinessByOwnerId(ownerId: String): Result<Business?>

    suspend fun getBusinessById(businessId: String): Result<Business?>

    suspend fun updateBusiness(business: Business): Result<Unit>

    suspend fun getAllBusinesses(): Result<List<Business>>

    /** Flujo en vivo de todos los negocios (refleja altas, modificaciones y bajas). */
    fun getAllBusinessesFlow(): Flow<List<Business>>
}
