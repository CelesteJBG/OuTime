package com.outime.app.domain.model

data class Service(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val description: String = "",
    val duration: Int = 0,
    val price: Double = 0.0
)