package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class InicioViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    @Test
    fun muestraElUsuarioYCierraSesion() = runTest {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.autenticar("supervisor", "1234") } returns Datos.supervisor
        val repositorio = UsuarioRepository(dao)
        repositorio.iniciarSesion("supervisor", "1234")
        val viewModel = InicioViewModel(repositorio)

        assertEquals(Datos.supervisor, viewModel.usuario.value)

        viewModel.cerrarSesion()

        assertNull(viewModel.usuario.value)
    }
}
