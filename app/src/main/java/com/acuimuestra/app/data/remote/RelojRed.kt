package com.acuimuestra.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

data class HoraRegistro(val millis: Long, val verificada: Boolean)

class RelojRed(
    private val servidorNtp: String = "time.google.com",
    private val servidorWeb: String = "https://www.google.com"
) {

    suspend fun ahora(): HoraRegistro = withContext(Dispatchers.IO) {
        val horaRed = runCatching { horaNtp() }.getOrNull()
            ?: runCatching { horaWeb() }.getOrNull()
        if (horaRed != null) HoraRegistro(horaRed, verificada = true)
        else HoraRegistro(System.currentTimeMillis(), verificada = false)
    }

    private fun horaNtp(): Long = DatagramSocket().use { socket ->
        socket.soTimeout = TIEMPO_ESPERA
        val paquete = ByteArray(48).apply { this[0] = 0x1B }
        val servidor = InetAddress.getByName(servidorNtp)
        socket.send(DatagramPacket(paquete, paquete.size, servidor, 123))
        socket.receive(DatagramPacket(paquete, paquete.size))
        val segundos = leer32(paquete, 40)
        val fraccion = leer32(paquete, 44)
        check(segundos > 0) { "Respuesta NTP vacía" }
        (segundos - SEGUNDOS_1900_A_1970) * 1000 + fraccion * 1000 / 0x1_0000_0000L
    }

    private fun horaWeb(): Long {
        val conexion = URL(servidorWeb).openConnection() as HttpURLConnection
        return try {
            conexion.requestMethod = "HEAD"
            conexion.connectTimeout = TIEMPO_ESPERA
            conexion.readTimeout = TIEMPO_ESPERA
            conexion.useCaches = false
            conexion.connect()
            conexion.date.takeIf { it > 0 } ?: error("Sin cabecera Date")
        } finally {
            conexion.disconnect()
        }
    }

    private fun leer32(datos: ByteArray, inicio: Int): Long =
        (0 until 4).fold(0L) { valor, i -> (valor shl 8) or (datos[inicio + i].toLong() and 0xFF) }

    private companion object {
        const val TIEMPO_ESPERA = 4_000
        const val SEGUNDOS_1900_A_1970 = 2_208_988_800L
    }
}