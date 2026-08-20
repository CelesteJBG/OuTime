package com.outime.app.presentation.util

import com.outime.app.presentation.model.BusinessCategory
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
 * Recurso de imagen local (empaquetado en el APK) para un negocio según su
 * CATEGORÍA. Delega en la fuente única [BusinessCategory]. Si la categoría no tiene
 * imagen asociada devuelve null y se mantiene el placeholder visual.
 */
fun businessThumbnailRes(category: String): Int? = BusinessCategory.bannerFor(category)