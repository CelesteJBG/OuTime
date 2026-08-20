package com.outime.app.presentation.util

import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la generación del QR: garantiza que el QR contiene exactamente el
 * appointmentId (nada más) y que ids distintos producen códigos distintos.
 */
class QrCodeUtilsTest {

    private fun decode(matrix: BitMatrix): String? {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] =
                    if (matrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        val source = RGBLuminanceSource(width, height, pixels)
        return QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source))).text
    }

    @Test
    fun `encodeQr genera una matriz para un appointmentId`() {
        val matrix = encodeQr("abc123xyz")
        assertNotNull(matrix)
        assertTrue(matrix!!.width > 0 && matrix.height > 0)
    }

    @Test
    fun `el QR se decodifica de vuelta al mismo appointmentId`() {
        val appointmentId = "abc123xyz"
        val matrix = encodeQr(appointmentId)!!
        assertEquals(appointmentId, decode(matrix))
    }

    @Test
    fun `distintos appointmentId producen QR distintos`() {
        val first = encodeQr("id-uno")!!
        val second = encodeQr("id-dos")!!
        // Solo aceptamos que "id-uno" no se decodifique como "id-dos".
        assertEquals("id-uno", decode(first))
        assertEquals("id-dos", decode(second))
    }
}