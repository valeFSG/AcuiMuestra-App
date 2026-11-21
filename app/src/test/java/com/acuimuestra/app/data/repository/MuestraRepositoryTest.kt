package com.acuimuestra.app.data.repository

import com.acuimuestra.app.Datos
import com.acuimuestra.app.data.local.MuestraDao
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Muestra
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class MuestraRepositoryTest {

    private val dao = mockk<MuestraDao>()
    private var cambios = 0
    private var usuario: String? = "supervisor"
    private val repositorio = MuestraRepository(dao, usuarioActual = { usuario }, alCambiar = { cambios++ })

    @Before
    fun preparar() {
        coEvery { dao.insertar(any()) } returns 15L
        coEvery { dao.actualizar(any()) } just runs
        coEvery { dao.actualizarEstado(any(), any(), any(), any()) } just runs
        coEvery { dao.corregir(any(), any(), any(), any(), any(), any(), any(), any(), any()) } just runs
    }

    @Test
    fun guardarNueva_laInsertaComoPendienteYAvisaParaSincronizar() = runTest {
        val insertada = slot<Muestra>()
        coEvery { dao.insertar(capture(insertada)) } returns 15L

        val id = repositorio.guardar(Datos.muestra(id = 0, sincronizada = true))

        assertEquals(15L, id)
        assertFalse(insertada.captured.sincronizada)
        assertEquals(1, cambios)
    }

    @Test
    fun guardarExistente_laActualiza() = runTest {
        val id = repositorio.guardar(Datos.muestra(id = 7))

        assertEquals(7L, id)
        coVerify { dao.actualizar(match { it.id == 7L && !it.sincronizada }) }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun revisar_guardaQuienRevisoYLimpiaElComentario() = runTest {
        repositorio.revisar(7, EstadoRevision.OBSERVADO, "  Repetir el conteo  ")
        repositorio.revisar(8, EstadoRevision.VALIDADO, "   ")

        coVerify { dao.actualizarEstado(7, EstadoRevision.OBSERVADO, "Repetir el conteo", "supervisor") }
        coVerify { dao.actualizarEstado(8, EstadoRevision.VALIDADO, null, "supervisor") }
        assertEquals(2, cambios)
    }

    @Test
    fun corregir_dejaLaMuestraComoCorregida() = runTest {
        usuario = "operador"

        repositorio.corregir(7, 190, 35, "Conteo repetido", 40.0, null, null)

        coVerify { dao.corregir(7, 190, 35, "Conteo repetido", 40.0, null, null, EstadoRevision.CORREGIDO, "operador") }
        assertEquals(1, cambios)
    }

    @Test
    fun consultas_delegaEnElDao() = runTest {
        val filtro = FiltroMuestras(centroId = 1, estado = EstadoRevision.PENDIENTE)
        every { dao.observarPorId(7) } returns flowOf(Datos.muestra())
        every { dao.filtrar(1, null, null, EstadoRevision.PENDIENTE, null, null) } returns flowOf(listOf(Datos.muestra()))
        every { dao.resumen(1, null, null, EstadoRevision.PENDIENTE, null, null) } returns flowOf(listOf(Datos.resumen(1)))
        every { dao.observarDetalle(7) } returns flowOf(Datos.detalle())
        every { dao.contarPendientes() } returns flowOf(3)

        assertEquals(7L, repositorio.observar(7).first()?.id)
        assertEquals(1, repositorio.filtrar(filtro).first().size)
        assertEquals(1, repositorio.resumen(filtro).first().size)
        assertEquals("u-7", repositorio.detalle(7).first()?.uuid)
        assertEquals(3, repositorio.pendientes().first())
    }

    @Test
    fun porDefecto_noFallaSinUsuarioNiAviso() = runTest {
        val simple = MuestraRepository(dao)
        simple.revisar(7, EstadoRevision.VALIDADO, null)
        coVerify { dao.actualizarEstado(7, EstadoRevision.VALIDADO, null, null) }
    }
}
