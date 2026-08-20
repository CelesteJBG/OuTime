package com.outime.app.presentation.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

private const val QR_TARGET_SIZE = 512
private const val QR_MARGIN = 1

/**
 * Genera la matriz del QR (puro, sin Android) a partir del [content].
 * Permite comprobar por tests que el QR contiene exactamente el appointmentId.
 */
fun encodeQr(content: String): BitMatrix? = try {
    val hints = mapOf(EncodeHintType.MARGIN to QR_MARGIN)
    QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_TARGET_SIZE, QR_TARGET_SIZE, hints)
} catch (_: Exception) {
    null
}

/**
 * Convierte el QR en un mapa de bits Android del tamaño indicado, listo para mostrarse.
 * El contenido del QR es únicamente el [appointmentId] (sin datos personales).
 */
fun qrCodeBitmap(appointmentId: String, sizePx: Int): Bitmap? {
    val matrix = encodeQr(appointmentId) ?: return null

    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (x in 0 until matrix.width) {
        for (y in 0 until matrix.height) {
            bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }

    return if (bitmap.width == sizePx) bitmap else Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true)
}