package com.outime.app.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas de [normalizeText]: la función que recorta espacios, pasa a
 * minúsculas y elimina diacríticos (acentos, diéresis, etc.) para
 * búsquedas y categorías.
 *
 * Ver [TextNormalizer.kt] para la implementación.
 */
class TextNormalizerTest {

    @Test
    fun `normalizeText elimina acentos y pasa a minusculas`() {
        assertEquals("peluqueria", normalizeText("Peluquería"))
        assertEquals("clinica dental", normalizeText("Clínica Dental"))
        assertEquals("fisioterapia", normalizeText("Fisioterapia"))
    }

    @Test
    fun `normalizeText recorta espacios y normaliza caracteres con enie`() {
        assertEquals("taller mecanico", normalizeText("  Taller Mecánico  "))
        assertEquals("estudio de tatuajes", normalizeText("Estudio de Tatuajes  "))
        assertEquals("nino nunez", normalizeText("  Niño Nuñez"))
    }

    @Test
    fun `normalizeText no altera texto ya normalizado`() {
        assertEquals("peluqueria", normalizeText("peluqueria"))
        assertEquals("taller", normalizeText("taller"))
    }

    @Test
    fun `normalizeText cadena vacia o solo espacios devuelve vacio`() {
        assertEquals("", normalizeText(""))
        assertEquals("", normalizeText("   "))
    }

    @Test
    fun `normalizeText mantiene guiones y numeros`() {
        assertEquals("123", normalizeText("123"))
        assertEquals("opcion-a", normalizeText("Opción-A"))
    }
}