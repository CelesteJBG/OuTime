package com.outime.app.domain.model

data class Service(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val description: String = "",
    val durationMinutes: Int = 30,
    val price: Double = 0.0,
    val isActive: Boolean = true  //Baja lógica

    /*
        Un servicio con isActive = false permanece en Firestore (histórico/estadísticas) pero
        no se muestra ni puede reservarse. El default true evita migrar los documentos ya existentes.

     */
)