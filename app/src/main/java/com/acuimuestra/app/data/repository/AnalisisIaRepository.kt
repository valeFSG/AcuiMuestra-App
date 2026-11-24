package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.remote.AcuiMuestraApi
import com.acuimuestra.app.data.remote.AnalisisIa
import retrofit2.HttpException
import java.io.IOException

class ErrorAnalisisIa(mensaje: String) : Exception(mensaje)

class AnalisisIaRepository(
    private val api: AcuiMuestraApi,
    private val usuarioActual: () -> String?
) {
    suspend fun analizar(uuid: String): Result<AnalisisIa> = try {
        Result.success(api.analizarConIa(usuarioActual() ?: USUARIO_POR_DEFECTO, uuid))
    } catch (e: HttpException) {
        Result.failure(ErrorAnalisisIa(mensajePara(e.code())))
    } catch (e: IOException) {
        Result.failure(ErrorAnalisisIa("Sin conexión con el servidor. Revisa internet e intenta de nuevo."))
    }

    companion object {
        private const val USUARIO_POR_DEFECTO = "supervisor"

        fun mensajePara(codigo: Int): String = when (codigo) {
            403 -> "Tu perfil no puede usar el análisis con IA."
            404 -> "La foto de esta muestra aún no está en el servidor. Sincroniza e intenta de nuevo."
            429 -> "Se alcanzó el límite gratuito de la IA. Intenta en unos minutos."
            503 -> "La IA está ocupada o no está configurada. Intenta en un momento."
            504 -> "La IA tardó demasiado en responder. Intenta de nuevo."
            else -> "No se pudo analizar la foto (error $codigo)."
        }
    }
}
