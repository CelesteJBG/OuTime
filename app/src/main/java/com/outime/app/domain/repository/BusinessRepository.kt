package com.outime.app.domain.repository

import com.outime.app.domain.model.Business

interface BusinessRepository {

    suspend fun createBusiness(business: Business): Result<Unit>

    suspend fun getBusinessByOwnerId(ownerId: String): Result<Business?>

    suspend fun getBusinessById(businessId: String): Result<Business?>

    suspend fun updateBusiness(business: Business): Result<Unit>

    suspend fun getAllBusinesses(): Result<List<Business>>
}
