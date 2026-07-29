package com.outime.app.domain.repository

import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule

interface ScheduleRepository {

    suspend fun getSchedule(businessId: String): Result<BusinessSchedule?>

    suspend fun saveSchedule(schedule: BusinessSchedule): Result<Unit>

    suspend fun getBlockedDates(businessId: String): Result<List<BlockedDate>>

    suspend fun addBlockedDate(blockedDate: BlockedDate): Result<Unit>

    suspend fun removeBlockedDate(blockedDateId: String): Result<Unit>
}