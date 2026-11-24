package com.acuimuestra.app.data.model

fun codigoMuestra(uuid: String): String {
    val limpio = uuid.replace("-", "").uppercase()
    val esHex = limpio.length >= 8 && limpio.take(8).all { it.isDigit() || it in 'A'..'F' }
    return if (esHex) limpio.take(4) + "-" + limpio.substring(4, 8) else uuid.uppercase()
}
