package com.outime.app.domain.repository

import com.outime.app.domain.model.Service

interface ServiceRepository {

    suspend fun createService(service: Service): Result<Unit>

    suspend fun getServicesByBusiness(businessId: String): Result<List<Service>>

    suspend fun migrateServiceBusinessId(oldBusinessId: String, newBusinessId: String): Result<Unit>
}
