package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.remote.AcuiMuestraApi
import com.acuimuestra.app.data.remote.AnalisisIa
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AnalisisIaRepositoryTest {

    private val api = mockk<AcuiMuestraApi>()
    private var usuario: String? = "analista"
    private val repositorio = AnalisisIaRepository(api) { usuario }

    private val resultado = AnalisisIa(180, false, "buena", "Choritos separados", 185, 2.7, false, "gemini-flash-latest")

    private fun errorHttp(codigo: Int) = HttpException(Response.error<Any>(codigo, "{}".toResponseBody(null)))

    @Test
    fun analizar_devuelveElResultadoConElUsuarioDeLaSesion() = runTest {
        coEvery { api.analizarConIa("analista", "u-7") } returns resultado

        assertEquals(resultado, repositorio.analizar("u-7").getOrNull())
    }

    @Test
    fun analizar_sinSesion_usaSupervisorPorDefecto() = runTest {
        usuario = null
        coEvery { api.analizarConIa(any(), any()) } returns resultado

        repositorio.analizar("u-7")

        coVerify { api.analizarConIa("supervisor", "u-7") }
    }

    @Test
    fun analizar_errorDelServidor_devuelveUnMensajeClaro() = runTest {
        coEvery { api.analizarConIa(any(), any()) } throws errorHttp(429)

        val error = repositorio.analizar("u-7").exceptionOrNull()

        assertTrue(error is ErrorAnalisisIa)
        assertEquals(AnalisisIaRepository.mensajePara(429), error?.message)
    }

    @Test
    fun analizar_sinRed_avisaQueNoHayConexion() = runTest {
        coEvery { api.analizarConIa(any(), any()) } throws IOException("sin red")

        val error = repositorio.analizar("u-7").exceptionOrNull()

        assertTrue(error?.message.orEmpty().startsWith("Sin conexión"))
    }

    @Test
    fun mensajePara_cubreLosCodigosConocidos() {
        listOf(403, 404, 429, 503, 504).forEach { codigo ->
            assertTrue(AnalisisIaRepository.mensajePara(codigo).isNotBlank())
        }
        assertEquals("No se pudo analizar la foto (error 500).", AnalisisIaRepository.mensajePara(500))
    }
}
