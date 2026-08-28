package com.outime.app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo

/**
 * Gestión de persistencia local para el onboarding inicial de OuTime.
 *
 * Responsabilidad exclusiva: determinar si el onboarding debe mostrarse
 * y marcarlo como completado.
 *
 * Soporta un modo DEBUG que fuerza la visualización en cada ejecución
 * mediante [FORCE_ONBOARDING], útil para desarrollo/pruebas sin borrar datos.
 */
object OnboardingPersistence {

    private const val PREFS_NAME = "outime_onboarding"
    private const val KEY_COMPLETED = "hasCompletedOnboarding"

    /**
     * Activa en DEBUG para forzar el onboarding en cada arranque.
     * En RELEASE esta constante no tiene efecto.
     * Cambiar a false para simular comportamiento de producción en debug.
     */
    private const val FORCE_ONBOARDING = true

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** @return true si el onboarding ya fue completado (producción). */
    fun isOnboardingCompleted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_COMPLETED, false)

    /** Marca el onboarding como completado. */
    fun setOnboardingCompleted(context: Context) {
        prefs(context).edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    /** @return true si la app se está ejecutando en modo depurable (debug). */
    private fun isDebug(context: Context): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /**
     * Determina si el onboarding debe mostrarse en el arranque.
     *
     * - DEBUG + FORCE_ONBOARDING → siempre true (forzar visualización).
     * - Otro caso → true solo si no se ha completado antes.
     */
    fun shouldShowOnboarding(context: Context): Boolean {
        return if (isDebug(context) && FORCE_ONBOARDING) true
        else !isOnboardingCompleted(context)
    }
}