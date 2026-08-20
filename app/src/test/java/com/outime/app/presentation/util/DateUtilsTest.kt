package com.outime.app.presentation.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Pruebas de regresión de la representación canónica de FECHAS DE CALENDARIO (medianoche
 * UTC) y su separación respecto a los instantes locales del negocio.
 *
 * Cubre los casos: seleccionar 16 -> guardar 16; sin desplazamiento en zonas +/-;
 * weekday correcto (el miércoles no bloquea el martes); y que una franja local de un día
 * dado se compara contra la fecha de calendario correcta.
 */
class DateUtilsTest {

    private lateinit var originalZone: TimeZone

    @Before
    fun saveDefaultZone() {
        originalZone = TimeZone.getDefault()
    }

    // Restauramos siempre la zona horaria original para no contaminar otros tests.
    @After
    fun restoreDefaultZone() {
        TimeZone.setDefault(originalZone)
    }

    // 16/09/2026 = miércoles (Calendar.WEDNESDAY); 15/09/2026 = martes (Calendar.TUESDAY).
    private fun utcMidnightOf(y: Int, m: Int, d: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(y, m, d, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun localDayOfMonth(ms: Long): Int =
        Calendar.getInstance().apply { timeInMillis = ms }.get(Calendar.DAY_OF_MONTH)

    @Test
    fun `utcMidnight is idempotent`() {
        val picker = utcMidnightOf(2026, 8, 16)
        assertEquals(picker, utcMidnight(picker))
    }

    @Test
    fun `selecting 16 saves 16 in negative zone`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        val picker = utcMidnightOf(2026, 8, 16)
        val stored = utcMidnight(picker)
        assertEquals(picker, stored)
        // Leer de vuelta (como desde Firestore) continúa representando el 16.
        assertEquals(utcMidnightOf(2026, 8, 16), utcMidnight(stored))
    }

    @Test
    fun `selecting 16 saves 16 in positive zone`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"))
        val picker = utcMidnightOf(2026, 8, 16)
        val stored = utcMidnight(picker)
        assertEquals(picker, stored)
        assertEquals(utcMidnightOf(2026, 8, 16), utcMidnight(stored))
    }

    @Test
    fun `localStartOfDay keeps the selected date in negative zone`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        val picker = utcMidnightOf(2026, 8, 16)
        val start = localStartOfDay(picker)
        assertEquals(16, localDayOfMonth(start))
        assertEquals(picker, utcMidnightOfLocalDate(start))
    }

    @Test
    fun `localStartOfDay keeps the selected date in positive zone`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"))
        val picker = utcMidnightOf(2026, 8, 16)
        val start = localStartOfDay(picker)
        assertEquals(16, localDayOfMonth(start))
        assertEquals(picker, utcMidnightOfLocalDate(start))
    }

    @Test
    fun `localEndOfDay belongs to the selected date`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        val picker = utcMidnightOf(2026, 8, 16)
        assertEquals(16, localDayOfMonth(localEndOfDay(picker)))
    }

    @Test
    fun `weekday of selected date is not shifted by timezone`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        assertEquals(Calendar.WEDNESDAY, dayOfWeekOfUtcDate(utcMidnightOf(2026, 8, 16)))
        assertEquals(Calendar.TUESDAY, dayOfWeekOfUtcDate(utcMidnightOf(2026, 8, 15)))

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"))
        assertEquals(Calendar.WEDNESDAY, dayOfWeekOfUtcDate(utcMidnightOf(2026, 8, 16)))
    }

    @Test
    fun `blocking a wednesday does not block the previous tuesday`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        val wedPicked = utcMidnightOf(2026, 8, 16) // miércoles
        val tuePicked = utcMidnightOf(2026, 8, 15) // martes
        val blocked = utcMidnight(wedPicked)

        assertTrue(utcMidnight(blocked) == wedPicked)
        assertFalse(utcMidnight(blocked) == tuePicked)
    }

    @Test
    fun `a local slot of a date maps to that date key and not the previous day`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        val wedPicked = utcMidnightOf(2026, 8, 16)

        // Franja local del día 16 a las 10:00 (instante local del negocio).
        val slotStart = Calendar.getInstance().apply {
            clear()
            set(2026, 8, 16, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dateKey = utcMidnightOfLocalDate(slotStart)
        assertEquals(wedPicked, dateKey)
    }

    @Test
    fun `same blocked date keeps same calendar date across zones`() {
        val picker = utcMidnightOf(2026, 8, 16)

        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"))
        val keyNeg = utcMidnightOfLocalDate(localStartOfDay(picker))

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"))
        val keyPos = utcMidnightOfLocalDate(localStartOfDay(picker))

        assertEquals(picker, keyNeg)
        assertEquals(picker, keyPos)
        assertEquals(keyNeg, keyPos)
    }
}