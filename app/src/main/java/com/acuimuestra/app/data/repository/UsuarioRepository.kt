package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.local.UsuarioDao
import com.acuimuestra.app.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsuarioRepository(private val dao: UsuarioDao) {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    suspend fun iniciarSesion(usuario: String, clave: String): Usuario? =
        dao.autenticar(usuario.trim(), clave).also { _usuarioActual.value = it }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}