package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val dao = mockk<UsuarioDao>()
    private lateinit var repositorio: UsuarioRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        coEvery { dao.autenticar(any(), any()) } returns null
        coEvery { dao.autenticar("operador", "1234") } returns Datos.operador
        repositorio = UsuarioRepository(dao)
        viewModel = LoginViewModel(repositorio)
    }

    @Test
    fun camposVacios_muestraErrorYNoConsultaLaBase() = runTest {
        viewModel.ingresar()

        assertEquals("Ingresa usuario y contraseña", viewModel.estado.value.error)
        coVerify(exactly = 0) { dao.autenticar(any(), any()) }
    }

    @Test
    fun claveIncorrecta_muestraError() = runTest {
        viewModel.onUsuarioChange("operador")
        viewModel.onClaveChange("9999")

        viewModel.ingresar()

        val estado = viewModel.estado.value
        assertEquals("Usuario o contraseña incorrectos", estado.error)
        assertFalse(estado.ingresoExitoso)
        assertFalse(estado.cargando)
        assertNull(repositorio.usuarioActual.value)
    }

    @Test
    fun credencialesCorrectas_ingresaYGuardaLaSesion() = runTest {
        viewModel.onUsuarioChange(" operador ")
        viewModel.onClaveChange("1234")

        viewModel.ingresar()

        assertTrue(viewModel.estado.value.ingresoExitoso)
        assertEquals(Datos.operador, repositorio.usuarioActual.value)
    }

    @Test
    fun escribir_limpiaElErrorYNavegacionReiniciaElIngreso() = runTest {
        viewModel.ingresar()
        viewModel.onUsuarioChange("o")
        assertNull(viewModel.estado.value.error)

        viewModel.onUsuarioChange("operador")
        viewModel.onClaveChange("1234")
        viewModel.ingresar()
        viewModel.navegacionRealizada()

        assertFalse(viewModel.estado.value.ingresoExitoso)
    }
}
