package com.outime.app.presentation.util

import com.outime.app.R
import java.text.Normalizer
import java.util.Locale

/**
 * Normaliza texto para búsquedas y categorías: recorta espacios, pasa a minúsculas
 * y elimina diacríticos (acentos). Ej: "Peluquería" -> "peluqueria".
 */
fun normalizeText(input: String): String {
    val trimmed = input.trim()
    val decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
    val sb = StringBuilder(trimmed.length)
    for (ch in decomposed) {
        if (Character.getType(ch) != Character.NON_SPACING_MARK.toInt()) sb.append(ch)
    }
    return sb.toString().lowercase(Locale("es", "ES"))
}

/**
 * Recurso de imagen local (empaquetado en el APK) para los negocios demo según su
 * nombre normalizado. Si no existe una imagen adecuada devuelve null y se mantiene
 * el placeholder visual.
 */
fun businessThumbnailRes(name: String): Int? {
    val n = normalizeText(name)
    return when {
        n.contains("clinica") -> R.drawable.servicio_clinica_dental
        n.contains("taller") -> R.drawable.servicio_taller_mecanico
        n.contains("fisio") -> R.drawable.servicio_fisioterapia
        n.contains("peluquer") -> R.drawable.banner_peluqueria_chicas
        n.contains("tatto") -> R.drawable.servicio_estudio_tatto
        else -> null
    }
}