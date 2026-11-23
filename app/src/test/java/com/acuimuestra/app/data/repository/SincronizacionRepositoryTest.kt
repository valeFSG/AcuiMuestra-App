package com.acuimuestra.app.data.repository

import com.acuimuestra.app.Datos
import com.acuimuestra.app.data.local.AlmacenFotos
import com.acuimuestra.app.data.local.MuestraDao
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.MuestraPendiente
import com.acuimuestra.app.data.model.Ubicacion
import com.acuimuestra.app.data.remote.AcuiMuestraApi
import com.acuimuestra.app.data.remote.CambioEstadoEnvio
import com.acuimuestra.app.data.remote.MuestraRemota
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SincronizacionRepositoryTest {

    private val muestraDao = mockk<MuestraDao>()
    private val usuarioDao = mockk<UsuarioDao>()
    private val api = mockk<AcuiMuestraApi>()
    private val fotos = mockk<AlmacenFotos>()
    private val repositorio = SincronizacionRepository(muestraDao, usuarioDao, api, fotos) { "operador" }

    private fun errorHttp(codigo: Int) = HttpException(Response.error<Any>(codigo, "{}".toResponseBody(null)))

    private fun pendiente(muestra: Muestra) = MuestraPendiente(
        muestra = muestra,
        centroNombre = "Centro Ficticio Norte",
        trenNombre = "Tren 1",
        lineaNombre = "Línea 1",
        operadorUsuario = "operador"
    )

    private fun remota(estado: EstadoRevision = EstadoRevision.VALIDADO, conteo: Int = 185) = MuestraRemota(
        id = "u-7",
        centro = "Centro Ficticio Norte",
        tren = "Tren 1",
        linea = "Línea 1",
        operador = "operador",
        fechaHora = 1_000,
        horaVerificada = true,
        tramoCm = 30,
        conteo = conteo,
        observaciones = "Sin daños",
        calibrePromedioMm = 42.5,
        pesoGramos = 1200.0,
        estado = estado,
        comentarioSupervisor = null
    )

    @Before
    fun preparar() {
        coEvery { muestraDao.marcarSincronizada(any()) } just runs
        coEvery { muestraDao.insertar(any()) } returns 20L
        coEvery { muestraDao.actualizar(any()) } just runs
        coEvery { muestraDao.buscarUbicacion(any(), any(), any()) } returns Ubicacion(1, 1, 1)
        coEvery { usuarioDao.buscarPorUsuario("operador") } returns Datos.operador
        coEvery { api.crear(any(), any()) } returns remota()
        coEvery { api.cambiarEstado(any(), any(), any()) } returns remota()
        coEvery { api.corregir(any(), any(), any()) } returns remota()
        coEvery { api.listar(any()) } returns emptyList()
        coEvery { api.subirFoto(any(), any(), any()) } just runs
        coEvery { fotos.leerParaEnviar(any()) } returns null
    }

    @Test
    fun enviar_muestraPendiente_laCreaYLaMarcaSincronizada() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra(estado = EstadoRevision.PENDIENTE)))

        assertEquals(1, repositorio.enviar())

        coVerify { api.crear("operador", match { it.id == "u-7" && it.conteo == 185 && it.fotoNombre == "muestra7.jpg" }) }
        coVerify(exactly = 0) { api.cambiarEstado(any(), any(), any()) }
        coVerify { muestraDao.marcarSincronizada("u-7") }
    }

    @Test
    fun enviar_muestraRevisada_enviaElCambioDeEstadoComoSupervisor() = runTest {
        val observada = Datos.muestra(estado = EstadoRevision.OBSERVADO).copy(comentarioSupervisor = "Repetir el conteo")
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(observada))

        repositorio.enviar()

        coVerify { api.cambiarEstado("supervisor", "u-7", CambioEstadoEnvio(EstadoRevision.OBSERVADO, "Repetir el conteo")) }
    }

    @Test
    fun enviar_muestraCorregida_usaPut() = runTest {
        val corregida = Datos.muestra(estado = EstadoRevision.CORREGIDO).copy(usuarioCambio = "operador", observaciones = "")
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(corregida))

        repositorio.enviar()

        coVerify { api.corregir("operador", "u-7", match { it.observaciones == null }) }
    }

    @Test
    fun enviar_siElServidorRechazaLosDatos_quedaPendiente() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra()))
        coEvery { api.crear(any(), any()) } throws errorHttp(400)

        assertEquals(0, repositorio.enviar())

        coVerify(exactly = 0) { muestraDao.marcarSincronizada(any()) }
    }

    @Test
    fun enviar_conflictoEnElEstado_igualSeMarcaSincronizada() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra(estado = EstadoRevision.VALIDADO)))
        coEvery { api.cambiarEstado(any(), any(), any()) } throws errorHttp(409)

        assertEquals(1, repositorio.enviar())

        coVerify { muestraDao.marcarSincronizada("u-7") }
    }

    @Test(expected = HttpException::class)
    fun enviar_errorDelServidor_seReintentaMasTarde() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra()))
        coEvery { api.crear(any(), any()) } throws errorHttp(500)

        repositorio.enviar()
    }

    @Test(expected = IOException::class)
    fun enviar_sinConexion_lanzaErrorParaReintentar() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra()))
        coEvery { api.crear(any(), any()) } throws IOException("sin red")

        repositorio.enviar()
    }

    @Test
    fun recibir_muestraNueva_laInsertaComoSincronizada() = runTest {
        coEvery { muestraDao.buscarPorUuid("u-7") } returns null
        coEvery { api.listar("operador") } returns listOf(remota())
        val insertada = slot<Muestra>()
        coEvery { muestraDao.insertar(capture(insertada)) } returns 20L

        assertEquals(1, repositorio.recibir())

        with(insertada.captured) {
            assertEquals("u-7", uuid)
            assertTrue(sincronizada)
            assertEquals(EstadoRevision.VALIDADO, estado)
            assertEquals(Datos.operador.id, operadorId)
            assertNull(fotoUri)
        }
    }

    @Test
    fun recibir_muestraSincronizadaConCambios_laActualizaYConservaLaFoto() = runTest {
        coEvery { muestraDao.buscarPorUuid("u-7") } returns Datos.muestra(sincronizada = true)
        coEvery { api.listar(any()) } returns listOf(remota(EstadoRevision.OBSERVADO))

        assertEquals(1, repositorio.recibir())

        coVerify {
            muestraDao.actualizar(match {
                it.id == 7L && it.estado == EstadoRevision.OBSERVADO && it.fotoUri == "file:///fotos/muestra7.jpg"
            })
        }
    }

    @Test
    fun recibir_noPisaCambiosLocalesNiDatosIguales() = runTest {
        val igual = Datos.muestra(sincronizada = true).copy(estado = EstadoRevision.VALIDADO)
        coEvery { api.listar(any()) } returns listOf(remota(), remota().copy(id = "u-8"))
        coEvery { muestraDao.buscarPorUuid("u-7") } returns igual
        coEvery { muestraDao.buscarPorUuid("u-8") } returns Datos.muestra(id = 8, uuid = "u-8", sincronizada = false)

        assertEquals(0, repositorio.recibir())

        coVerify(exactly = 0) { muestraDao.actualizar(any()) }
        coVerify(exactly = 0) { muestraDao.insertar(any()) }
    }

    @Test
    fun recibir_omiteUbicacionesOUsuariosDesconocidos() = runTest {
        coEvery { muestraDao.buscarPorUuid(any()) } returns null
        coEvery { api.listar(any()) } returns listOf(remota(), remota().copy(id = "u-9", operador = "otro"))
        coEvery { muestraDao.buscarUbicacion(any(), any(), any()) } returnsMany listOf(null, Ubicacion(1, 1, 1))
        coEvery { usuarioDao.buscarPorUsuario("otro") } returns null

        assertEquals(0, repositorio.recibir())
    }

    @Test
    fun sincronizar_enviaYRecibe() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra()))
        coEvery { muestraDao.buscarPorUuid("u-7") } returns null
        coEvery { api.listar(any()) } returns listOf(remota())

        assertEquals(ResultadoSincronizacion(enviadas = 1, recibidas = 1), repositorio.sincronizar())
    }

    @Test
    fun enviar_conFoto_laSubeComoOperador() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra()))
        coEvery { fotos.leerParaEnviar("file:///fotos/muestra7.jpg") } returns byteArrayOf(1, 2, 3)

        repositorio.enviar()

        coVerify { api.subirFoto("operador", "u-7", match { it.contentLength() == 3L }) }
        coVerify { muestraDao.marcarSincronizada("u-7") }
    }

    @Test
    fun enviar_fotoIlegible_noLaSubePeroSincronizaLosDatos() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra()))

        assertEquals(1, repositorio.enviar())

        coVerify(exactly = 0) { api.subirFoto(any(), any(), any()) }
    }

    @Test
    fun enviar_sinFoto_noIntentaLeerla() = runTest {
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(Datos.muestra().copy(fotoUri = null)))

        repositorio.enviar()

        coVerify(exactly = 0) { fotos.leerParaEnviar(any()) }
    }

    @Test
    fun recibir_muestraConFoto_laDescargaYLaGuarda() = runTest {
        coEvery { muestraDao.buscarPorUuid("u-7") } returns null
        coEvery { api.listar(any()) } returns listOf(remota().copy(tieneFoto = true))
        coEvery { api.descargarFoto("operador", "u-7") } returns "jpg".toResponseBody(null)
        coEvery { fotos.guardarRecibida("u-7", any()) } returns "content://fotos/remota_u-7.jpg"
        val insertada = slot<Muestra>()
        coEvery { muestraDao.insertar(capture(insertada)) } returns 20L

        repositorio.recibir()

        assertEquals("content://fotos/remota_u-7.jpg", insertada.captured.fotoUri)
    }

    @Test
    fun recibir_fotoNoDisponible_insertaSinFoto() = runTest {
        coEvery { muestraDao.buscarPorUuid(any()) } returns null
        coEvery { api.listar(any()) } returns listOf(
            remota().copy(tieneFoto = true),
            remota().copy(id = "u-8", tieneFoto = true)
        )
        coEvery { api.descargarFoto(any(), "u-7") } throws errorHttp(404)
        coEvery { api.descargarFoto(any(), "u-8") } throws IOException("sin red")
        val insertadas = mutableListOf<Muestra>()
        coEvery { muestraDao.insertar(capture(insertadas)) } returns 20L

        assertEquals(2, repositorio.recibir())

        assertTrue(insertadas.all { it.fotoUri == null })
    }

    @Test
    fun recibir_conFotoLocal_noLaDescargaDeNuevo() = runTest {
        coEvery { muestraDao.buscarPorUuid("u-7") } returns Datos.muestra(sincronizada = true)
        coEvery { api.listar(any()) } returns listOf(remota().copy(tieneFoto = true))

        repositorio.recibir()

        coVerify(exactly = 0) { api.descargarFoto(any(), any()) }
    }

    @Test
    fun enviar_muestraRevisada_usaElSupervisorQueLaReviso() = runTest {
        val validada = Datos.muestra(estado = EstadoRevision.VALIDADO).copy(revisadoPor = "supervisora2", usuarioCambio = "operador")
        coEvery { muestraDao.pendientes() } returns listOf(pendiente(validada))

        repositorio.enviar()

        coVerify { api.cambiarEstado("supervisora2", "u-7", any()) }
    }

    @Test
    fun recibir_guardaQuienRevisoLaMuestra() = runTest {
        coEvery { muestraDao.buscarPorUuid("u-7") } returns null
        coEvery { api.listar(any()) } returns listOf(remota().copy(revisadoPor = "supervisor"))
        val insertada = slot<Muestra>()
        coEvery { muestraDao.insertar(capture(insertada)) } returns 20L

        repositorio.recibir()

        assertEquals("supervisor", insertada.captured.revisadoPor)
    }
}
