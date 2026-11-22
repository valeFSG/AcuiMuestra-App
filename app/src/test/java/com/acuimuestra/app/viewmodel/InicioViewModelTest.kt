package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.remote.ActualMarina
import com.acuimuestra.app.data.remote.MarinaApi
import com.acuimuestra.app.data.remote.RespuestaMarina
import com.acuimuestra.app.data.repository.CondicionMarina
import com.acuimuestra.app.data.repository.MarinaRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class InicioViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val usuarioDao = mockk<UsuarioDao>()
    private val marinaApi = mockk<MarinaApi>()
    private val respuesta = RespuestaMarina(ActualMarina("2026-11-22T10:00", 0.8, 11.2))

    private fun crearViewModel() = InicioViewModel(UsuarioRepository(usuarioDao), MarinaRepository(marinaApi))

    @Test
    fun muestraElUsuarioYCierraSesion() = runTest {
        coEvery { usuarioDao.autenticar("supervisor", "1234") } returns Datos.supervisor
        coEvery { marinaApi.condicionesActuales(any(), any(), any(), any()) } returns respuesta
        val repositorio = UsuarioRepository(usuarioDao)
        repositorio.iniciarSesion("supervisor", "1234")
        val viewModel = InicioViewModel(repositorio, MarinaRepository(marinaApi))

        assertEquals(Datos.supervisor, viewModel.usuario.value)

        viewModel.cerrarSesion()

        assertNull(viewModel.usuario.value)
    }

    @Test
    fun alIniciar_cargaLasCondicionesDelMar() = runTest {
        coEvery { marinaApi.condicionesActuales(any(), any(), any(), any()) } returns respuesta

        val viewModel = crearViewModel()

        assertEquals(EstadoMarina.Listo(CondicionMarina(0.8, 11.2, "2026-11-22T10:00")), viewModel.marina.value)
    }

    @Test
    fun sinConexion_quedaSinDatosYAlReintentarSeRecupera() = runTest {
        coEvery { marinaApi.condicionesActuales(any(), any(), any(), any()) } throws IOException("sin red")
        val viewModel = crearViewModel()

        assertEquals(EstadoMarina.SinDatos, viewModel.marina.value)

        coEvery { marinaApi.condicionesActuales(any(), any(), any(), any()) } returns respuesta
        viewModel.cargarMarina()

        assertEquals(EstadoMarina.Listo(CondicionMarina(0.8, 11.2, "2026-11-22T10:00")), viewModel.marina.value)
    }
}