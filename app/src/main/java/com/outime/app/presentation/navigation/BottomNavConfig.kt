package com.outime.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo de un destino de la Bottom Navigation.
 * Reutilizable para cualquier rol mediante diferentes listas de configuración.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Destinos de la Bottom Navigation para el rol BUSINESS.
 * Estructura: Inicio → Citas → Servicios → Horario → Perfil
 */
fun businessNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        route = Routes.BUSINESS_HOME,
        label = "Inicio",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = Routes.BUSINESS_APPOINTMENTS,
        label = "Citas",
        icon = Icons.Default.CalendarMonth
    ),
    BottomNavItem(
        route = Routes.BUSINESS_SERVICES,
        label = "Servicios",
        icon = Icons.Default.Build
    ),
    BottomNavItem(
        route = Routes.SCHEDULE_MANAGEMENT,
        label = "Horario",
        icon = Icons.Default.Schedule
    ),
    BottomNavItem(
        route = Routes.BUSINESS_PROFILE,
        label = "Perfil",
        icon = Icons.Default.Person
    )
)

/**
 * Destinos de la Bottom Navigation para el rol CLIENT.
 * Estructura: Inicio → Citas → Perfil
 * No se incluye "Explorar" porque ClientHomeScreen ya contiene la funcionalidad
 * completa de búsqueda/categorías/negocios.
 * No se incluye "Alertas" porque no existe infraestructura de notificaciones.
 */
fun clientNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        route = Routes.CLIENT_HOME,
        label = "Inicio",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = Routes.CLIENT_APPOINTMENTS,
        label = "Citas",
        icon = Icons.Default.CalendarMonth
    ),
    BottomNavItem(
        route = Routes.CLIENT_PROFILE,
        label = "Perfil",
        icon = Icons.Default.Person
    )
)

/**
 * Conjunto de rutas "main" por rol.
 * Se usa para determinar cuándo mostrar la Bottom Navigation y qué
 * configuración de items aplicar.
 */
val businessMainRoutes: Set<String> = setOf(
    Routes.BUSINESS_HOME,
    Routes.BUSINESS_APPOINTMENTS,
    Routes.BUSINESS_SERVICES,
    Routes.SCHEDULE_MANAGEMENT,
    Routes.BUSINESS_PROFILE
)

val clientMainRoutes: Set<String> = setOf(
    Routes.CLIENT_HOME,
    Routes.CLIENT_APPOINTMENTS,
    Routes.CLIENT_PROFILE
)