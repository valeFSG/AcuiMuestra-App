package com.acuimuestra.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.model.Rol
import com.acuimuestra.app.data.model.Usuario
import com.acuimuestra.app.data.repository.UsuarioRepository
import com.acuimuestra.app.ui.screens.LoginScreen
import com.acuimuestra.app.ui.theme.AcuiMuestraTheme
import com.acuimuestra.app.viewmodel.LoginViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule val compose = createComposeRule()

    private val operador = Usuario(1, "operador", "1234", "Camila Rojas", Rol.OPERADOR)

    private val daoFalso = object : UsuarioDao {
        override suspend fun autenticar(usuario: String, clave: String) =
            if (usuario == "operador" && clave == "1234") operador else null
        override suspend fun obtenerPorId(id: Long): Usuario? = null
        override suspend fun buscarPorUsuario(usuario: String): Usuario? = null
        override suspend fun contar() = 1
        override suspend fun insertarTodos(usuarios: List<Usuario>) = Unit
    }

    private fun mostrarLogin(onLoginExitoso: () -> Unit = {}) {
        compose.setContent {
            AcuiMuestraTheme {
                LoginScreen(onLoginExitoso = onLoginExitoso, viewModel = LoginViewModel(UsuarioRepository(daoFalso)))
            }
        }
    }

    @Test
    fun muestraLosCuatroPerfiles() {
        mostrarLogin()

        compose.onNodeWithText("Camila Rojas").assertIsDisplayed()
        compose.onNodeWithText("Diego Muñoz").assertIsDisplayed()
        compose.onNodeWithText("Andrea Soto").assertIsDisplayed()
        compose.onNodeWithText("Felipe Vargas").assertIsDisplayed()
    }

    @Test
    fun claveVacia_muestraMensajeDeError() {
        mostrarLogin()

        compose.onNodeWithText("Camila Rojas").performClick()
        compose.onNodeWithText("Ingresar").performClick()

        compose.onNodeWithText("Ingresa usuario y contraseña").assertIsDisplayed()
    }

    @Test
    fun claveIncorrecta_muestraMensajeDeError() {
        mostrarLogin()

        compose.onNodeWithText("Camila Rojas").performClick()
        compose.onNode(hasSetTextAction() and hasText("Contraseña")).performTextInput("9999")
        compose.onNodeWithText("Ingresar").performClick()

        compose.onNodeWithText("Usuario o contraseña incorrectos").assertIsDisplayed()
    }

    @Test
    fun credencialesCorrectas_ingresan() {
        var ingreso = false
        mostrarLogin { ingreso = true }

        compose.onNodeWithText("Camila Rojas").performClick()
        compose.onNode(hasSetTextAction() and hasText("Contraseña")).performTextInput("1234")
        compose.onNodeWithText("Ingresar").performClick()

        compose.waitUntil(timeoutMillis = 3_000) { ingreso }
    }

    @Test
    fun cambiarPerfil_vuelveALaLista() {
        mostrarLogin()

        compose.onNodeWithText("Camila Rojas").performClick()
        compose.onNodeWithText("Cambiar perfil").performClick()

        compose.onNodeWithText("Felipe Vargas").assertIsDisplayed()
    }
}