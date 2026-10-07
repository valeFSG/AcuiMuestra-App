package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.Usuario
import com.acuimuestra.app.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.StateFlow

class InicioViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    val usuario: StateFlow<Usuario?> = usuarioRepository.usuarioActual

    fun cerrarSesion() = usuarioRepository.cerrarSesion()

    companion object {
        val Factory = viewModelFactory {
            initializer { InicioViewModel(contenedor().usuarioRepository) }
        }
    }
}