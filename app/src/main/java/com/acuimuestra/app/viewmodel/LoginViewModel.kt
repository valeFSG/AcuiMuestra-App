package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val ingresoExitoso: Boolean = false
)

class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onUsuarioChange(valor: String) = _estado.update { it.copy(usuario = valor, error = null) }

    fun onClaveChange(valor: String) = _estado.update { it.copy(clave = valor, error = null) }

    fun ingresar() {
        val actual = _estado.value
        if (actual.usuario.isBlank() || actual.clave.isBlank()) {
            _estado.update { it.copy(error = "Ingresa usuario y contraseña") }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val usuario = usuarioRepository.iniciarSesion(actual.usuario, actual.clave)
            _estado.update {
                if (usuario != null) it.copy(cargando = false, ingresoExitoso = true)
                else it.copy(cargando = false, error = "Usuario o contraseña incorrectos")
            }
        }
    }

    fun navegacionRealizada() = _estado.update { it.copy(ingresoExitoso = false) }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(contenedor().usuarioRepository) }
        }
    }
}