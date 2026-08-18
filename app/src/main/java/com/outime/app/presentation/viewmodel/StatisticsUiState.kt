package com.outime.app.presentation.viewmodel

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val selectedPeriod: StatPeriod = StatPeriod.MONTH,
    val statistics: StatisticsData = StatisticsData()
)
