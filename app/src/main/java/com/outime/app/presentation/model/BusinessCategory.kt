package com.outime.app.presentation.model

import androidx.annotation.DrawableRes
import com.outime.app.R
import com.outime.app.domain.model.Business
import com.outime.app.presentation.util.normalizeText

/**
 * Fuente única de categorías de negocio.
 *
 * Cada categoría expone:
 *  - [label]: valor visible / canónico que se guarda en `Business.category`.
 *  - [imageRes]: imagen/banner asociado a la categoría.
 *
 * La imagen de un negocio se determina SIEMPRE por su categoría, nunca por su nombre.
 * [bannerFor] es tolerante con textos existentes en Firestore (categorías en texto
 * libre) mediante coincidencia por palabras clave normalizadas.
 */
enum class BusinessCategory(
    val label: String,
    @DrawableRes val imageRes: Int?
) {
    PELUQUERIA("Peluquería", R.drawable.banner_peluqueria_chicas),
    CLINICA_DENTAL("Clínica dental", R.drawable.banner_clinica_dental),
    TALLER_MECANICO("Taller mecánico", R.drawable.banner_taller_mecanico),
    ESTUDIO_TATUAJES("Estudio de tatuajes", R.drawable.banner_estudio_tatto),
    FISIOTERAPIA("Fisioterapia", R.drawable.banner_fisioterapia);

    companion object {

        /** Categorías seleccionables y su orden de presentación. */
        val categories: List<BusinessCategory> = entries.toList()

        /** Resuelve el label canónico a partir del texto guardado en Firestore, si coincide. */
        fun fromStored(stored: String): BusinessCategory? =
            categories.firstOrNull { it.label.equals(stored, ignoreCase = true) }

        /** Imagen/banner según la categoría. Devuelve null si no hay imagen asociada. */
        @DrawableRes
        fun bannerFor(category: String): Int? {
            val n = normalizeText(category)
            return when {
                n.contains("peluquer") -> R.drawable.banner_peluqueria_chicas
                n.contains("clinica") -> R.drawable.banner_clinica_dental
                n.contains("taller") -> R.drawable.banner_taller_mecanico
                n.contains("tatua") -> R.drawable.banner_estudio_tatto
                n.contains("fisio") -> R.drawable.banner_fisioterapia
                else -> null
            }
        }

        /** Delegado útil para las tarjetas que renderizan un [Business]. */
        @DrawableRes
        fun bannerFor(business: Business): Int? = bannerFor(business.category)
    }
}