package com.outime.app.presentation.viewmodel

import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.presentation.model.TimeSlot

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val schedule: BusinessSchedule? = null,
    val blockedDates: List<BlockedDate> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList()
)