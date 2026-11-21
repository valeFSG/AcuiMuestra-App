package com.acuimuestra.app.data.repository

import com.acuimuestra.app.Datos
import com.acuimuestra.app.data.local.CatalogoDao
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.Linea
import com.acuimuestra.app.data.model.Tren
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuarioYCatalogoRepositoryTest {

    @Test
    fun iniciarSesion_quitaEspaciosYGuardaElUsuario() = runTest {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.autenticar("operador", "1234") } returns Datos.operador
        val repositorio = UsuarioRepository(dao)

        assertEquals(Datos.operador, repositorio.iniciarSesion("  operador ", "1234"))
        assertEquals(Datos.operador, repositorio.usuarioActual.value)

        repositorio.cerrarSesion()
        assertNull(repositorio.usuarioActual.value)
    }

    @Test
    fun catalogo_entregaCentrosTrenesYLineas() = runTest {
        val dao = mockk<CatalogoDao>()
        every { dao.centros() } returns flowOf(listOf(Centro(1, "Centro Ficticio Norte")))
        every { dao.trenesDeCentro(1) } returns flowOf(listOf(Tren(2, 1, "Tren 1")))
        every { dao.lineasDeTren(2) } returns flowOf(listOf(Linea(3, 2, "Línea 1")))
        val repositorio = CatalogoRepository(dao)

        assertEquals("Centro Ficticio Norte", repositorio.centros().first().single().nombre)
        assertEquals(2L, repositorio.trenes(1).first().single().id)
        assertEquals(3L, repositorio.lineas(2).first().single().id)
    }
}
