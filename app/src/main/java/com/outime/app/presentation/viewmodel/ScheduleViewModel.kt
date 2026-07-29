package com.outime.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outime.app.domain.model.Appointment
import com.outime.app.domain.model.BlockedDate
import com.outime.app.domain.model.BusinessSchedule
import com.outime.app.domain.model.DaySchedule
import com.outime.app.domain.repository.ScheduleRepository
import com.outime.app.presentation.model.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    /**
     * Carga el horario del negocio. Si no existe, se usa un horario por defecto
     * (L-V, mañana 09:00-14:00, tarde 16:00-20:00).
     */
    fun loadSchedule(businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = scheduleRepository.getSchedule(businessId)

            result.fold(
                onSuccess = { schedule ->
                    val effectiveSchedule = schedule ?: defaultSchedule(businessId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        schedule = effectiveSchedule
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun saveSchedule(schedule: BusinessSchedule) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = scheduleRepository.saveSchedule(schedule)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        schedule = schedule
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun loadBlockedDates(businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = scheduleRepository.getBlockedDates(businessId)

            result.fold(
                onSuccess = { blockedDates ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        blockedDates = blockedDates
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun addBlockedDate(businessId: String, dateMillis: Long, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val blockedDate = BlockedDate(
                businessId = businessId,
                date = dateMillis,
                reason = reason
            )

            val result = scheduleRepository.addBlockedDate(blockedDate)

            result.fold(
                onSuccess = {
                    // Recargar lista de fechas bloqueadas
                    loadBlockedDates(businessId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun removeBlockedDate(blockedDateId: String, businessId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = scheduleRepository.removeBlockedDate(blockedDateId)

            result.fold(
                onSuccess = {
                    loadBlockedDates(businessId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /**
     * Genera las franjas horarias para un día concreto.
     *
     * Validaciones:
     * - No genera franjas cuya hora de fin supere el final del turno.
     * - No genera franjas que se solapen parcialmente con citas existentes.
     * - No genera franjas en fechas pasadas (hora actual > inicio franja).
     * - No genera franjas si el día está cerrado o bloqueado.
     *
     * @param schedule Horario del negocio.
     * @param dateMillis Día seleccionado (epoch ms a medianoche, zona local).
     * @param durationMinutes Duración del servicio seleccionado.
     * @param existingAppointments Citas ya existentes del negocio para ese día.
     */
    fun generateTimeSlots(
        schedule: BusinessSchedule,
        dateMillis: Long,
        durationMinutes: Int,
        existingAppointments: List<Appointment>
    ): List<TimeSlot> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            // Normalizar a medianoche
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Dom..7=Sáb

        val daySchedule = schedule.weeklyHours[dayOfWeek]
        if (daySchedule == null || !daySchedule.isOpen) {
            return emptyList()
        }

        val slots = mutableListOf<TimeSlot>()
        val now = System.currentTimeMillis()

        // Turno de mañana
        if (daySchedule.morningStart.isNotBlank() && daySchedule.morningEnd.isNotBlank()) {
            slots.addAll(
                generateSlotsForShift(
                    calendar = calendar,
                    startStr = daySchedule.morningStart,
                    endStr = daySchedule.morningEnd,
                    durationMinutes = durationMinutes,
                    existingAppointments = existingAppointments,
                    now = now
                )
            )
        }

        // Turno de tarde
        if (daySchedule.afternoonStart.isNotBlank() && daySchedule.afternoonEnd.isNotBlank()) {
            slots.addAll(
                generateSlotsForShift(
                    calendar = calendar,
                    startStr = daySchedule.afternoonStart,
                    endStr = daySchedule.afternoonEnd,
                    durationMinutes = durationMinutes,
                    existingAppointments = existingAppointments,
                    now = now
                )
            )
        }

        return slots
    }

    /**
     * Genera franjas para un turno concreto (mañana o tarde).
     */
    private fun generateSlotsForShift(
        calendar: Calendar,
        startStr: String,
        endStr: String,
        durationMinutes: Int,
        existingAppointments: List<Appointment>,
        now: Long
    ): List<TimeSlot> {
        val startMinutes = parseTimeToMinutes(startStr) ?: return emptyList()
        val endMinutes = parseTimeToMinutes(endStr) ?: return emptyList()

        if (endMinutes <= startMinutes || durationMinutes <= 0) {
            return emptyList()
        }

        val slots = mutableListOf<TimeSlot>()
        var currentMinute = startMinutes

        while (currentMinute + durationMinutes <= endMinutes) {
            val slotStartCalendar = calendar.clone() as Calendar
            slotStartCalendar.set(Calendar.HOUR_OF_DAY, currentMinute / 60)
            slotStartCalendar.set(Calendar.MINUTE, currentMinute % 60)

            val slotEndCalendar = calendar.clone() as Calendar
            val endMinute = currentMinute + durationMinutes
            slotEndCalendar.set(Calendar.HOUR_OF_DAY, endMinute / 60)
            slotEndCalendar.set(Calendar.MINUTE, endMinute % 60)

            val slotStartMillis = slotStartCalendar.timeInMillis
            val slotEndMillis = slotEndCalendar.timeInMillis

            // Validación: no fechas pasadas
            val isPast = slotStartMillis <= now

            // Validación: no solapamiento con citas existentes
            val isOverlapping = existingAppointments.any { appointment ->
                isOverlapping(slotStartMillis, slotEndMillis, appointment, durationMinutes)
            }

            slots.add(
                TimeSlot(
                    startMillis = slotStartMillis,
                    endMillis = slotEndMillis,
                    isAvailable = !isPast && !isOverlapping
                )
            )

            currentMinute += durationMinutes
        }

        return slots
    }

    /**
     * Comprueba si una franja [slotStart, slotEnd) se solapa con una cita existente.
     * Se considera solapamiento si hay intersección parcial o total entre ambos intervalos.
     */
    private fun isOverlapping(
        slotStart: Long,
        slotEnd: Long,
        appointment: Appointment,
        durationMinutes: Int
    ): Boolean {
        val aptStart = appointment.dateTime
        val aptEnd = aptStart + durationMinutes * 60_000L
        return slotStart < aptEnd && aptStart < slotEnd
    }

    private fun parseTimeToMinutes(time: String): Int? {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return null
            val hours = parts[0].toIntOrNull() ?: return null
            val minutes = parts[1].toIntOrNull() ?: return null
            hours * 60 + minutes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Horario por defecto: Lunes-Viernes, mañana 09:00-14:00, tarde 16:00-20:00.
     */
    private fun defaultSchedule(businessId: String): BusinessSchedule {
        val workingDay = DaySchedule(
            isOpen = true,
            morningStart = "09:00",
            morningEnd = "14:00",
            afternoonStart = "16:00",
            afternoonEnd = "20:00"
        )

        return BusinessSchedule(
            businessId = businessId,
            weeklyHours = mapOf(
                Calendar.MONDAY to workingDay,
                Calendar.TUESDAY to workingDay,
                Calendar.WEDNESDAY to workingDay,
                Calendar.THURSDAY to workingDay,
                Calendar.FRIDAY to workingDay
            )
        )
    }

    fun updateTimeSlots(slots: List<TimeSlot>) {
        _uiState.value = _uiState.value.copy(timeSlots = slots)
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isSuccess = false,
            error = null
        )
    }
}