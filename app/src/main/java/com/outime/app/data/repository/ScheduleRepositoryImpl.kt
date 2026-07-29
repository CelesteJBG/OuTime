package com.outime.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.domain.model.DaySchedule
import com.outime.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.tasks.await

class ScheduleRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ScheduleRepository {

    companion object {
        private const val SCHEDULES_COLLECTION = "business_schedules"
        private const val BLOCKED_DATES_COLLECTION = "blocked_dates"
    }

    override suspend fun getSchedule(businessId: String): Result<BusinessSchedule?> = try {
        val snapshot = firestore
            .collection(SCHEDULES_COLLECTION)
            .document(businessId)
            .get()
            .await()

        if (!snapshot.exists()) {
            Result.success(null)
        } else {
            // Firestore no soporta Map<Int, *> directamente: usamos String como clave puente
            val raw = snapshot.data ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val rawWeekly = raw["weeklyHours"] as? Map<String, Any> ?: emptyMap()
            val weeklyHours = rawWeekly.mapNotNull { (key, value) ->
                val dayKey = key.toIntOrNull() ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                val dayMap = value as? Map<String, Any> ?: return@mapNotNull null
                dayKey to mapToDaySchedule(dayMap)
            }.toMap()

            Result.success(
                BusinessSchedule(
                    businessId = businessId,
                    weeklyHours = weeklyHours
                )
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveSchedule(schedule: BusinessSchedule): Result<Unit> = try {
        // Convertimos claves Int -> String para compatibilidad con Firestore
        val weeklyHoursStr = schedule.weeklyHours.mapKeys { it.key.toString() }
            .mapValues { dayScheduleToMap(it.value) }

        val data = mapOf(
            "businessId" to schedule.businessId,
            "weeklyHours" to weeklyHoursStr
        )

        firestore
            .collection(SCHEDULES_COLLECTION)
            .document(schedule.businessId)
            .set(data)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getBlockedDates(businessId: String): Result<List<BlockedDate>> = try {
        val snapshot = firestore
            .collection(BLOCKED_DATES_COLLECTION)
            .whereEqualTo("businessId", businessId)
            .get()
            .await()

        val blockedDates = snapshot.documents.mapNotNull { doc ->
            doc.toObject(BlockedDate::class.java)?.copy(id = doc.id)
        }

        Result.success(blockedDates)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addBlockedDate(blockedDate: BlockedDate): Result<Unit> = try {
        val docRef = firestore
            .collection(BLOCKED_DATES_COLLECTION)
            .document()

        val withId = blockedDate.copy(id = docRef.id)
        docRef.set(withId).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeBlockedDate(blockedDateId: String): Result<Unit> = try {
        firestore
            .collection(BLOCKED_DATES_COLLECTION)
            .document(blockedDateId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun mapToDaySchedule(map: Map<String, Any>): DaySchedule = DaySchedule(
        isOpen = map["isOpen"] as? Boolean ?: false,
        morningStart = map["morningStart"] as? String ?: "",
        morningEnd = map["morningEnd"] as? String ?: "",
        afternoonStart = map["afternoonStart"] as? String ?: "",
        afternoonEnd = map["afternoonEnd"] as? String ?: ""
    )

    private fun dayScheduleToMap(day: DaySchedule): Map<String, Any> = mapOf(
        "isOpen" to day.isOpen,
        "morningStart" to day.morningStart,
        "morningEnd" to day.morningEnd,
        "afternoonStart" to day.afternoonStart,
        "afternoonEnd" to day.afternoonEnd
    )
}