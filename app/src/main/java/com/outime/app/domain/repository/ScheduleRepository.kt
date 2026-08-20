package com.outime.app.domain.repository

import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    suspend fun getSchedule(businessId: String): Result<BusinessSchedule?>

    suspend fun saveSchedule(schedule: BusinessSchedule): Result<Unit>

    suspend fun getBlockedDates(businessId: String): Result<List<BlockedDate>>

    suspend fun addBlockedDate(blockedDate: BlockedDate): Result<Unit>

    suspend fun removeBlockedDate(blockedDateId: String): Result<Unit>

    /**
     * Escucha en tiempo real los cambios del horario semanal del negocio.
     * Emite [null] si el negocio aún no tiene horario guardado.
     */
    fun observeSchedule(businessId: String): Flow<BusinessSchedule?>

    /**
     * Escucha en tiempo real las fechas bloqueadas del negocio.
     * Se actualiza automáticamente cuando el negocio añade o elimina una
     * fecha bloqueada, sin necesidad de re-entrar en la pantalla.
     */
    fun observeBlockedDates(businessId: String): Flow<List<BlockedDate>>
}