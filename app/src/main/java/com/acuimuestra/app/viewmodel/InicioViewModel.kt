package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.Usuario
import com.acuimuestra.app.data.repository.CondicionMarina
import com.acuimuestra.app.data.repository.MarinaRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoMarina {
    data object Cargando : EstadoMarina
    data class Listo(val condicion: CondicionMarina) : EstadoMarina
    data object SinDatos : EstadoMarina
}

class InicioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val marinaRepository: MarinaRepository
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = usuarioRepository.usuarioActual

    private val _marina = MutableStateFlow<EstadoMarina>(EstadoMarina.Cargando)
    val marina: StateFlow<EstadoMarina> = _marina.asStateFlow()

    init {
        cargarMarina()
    }

    fun cargarMarina() {
        _marina.value = EstadoMarina.Cargando
        viewModelScope.launch {
            _marina.value = marinaRepository.condicionesActuales().fold(
                onSuccess = { EstadoMarina.Listo(it) },
                onFailure = { EstadoMarina.SinDatos }
            )
        }
    }

    fun cerrarSesion() = usuarioRepository.cerrarSesion()

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                InicioViewModel(contenedor.usuarioRepository, contenedor.marinaRepository)
            }
        }
    }
}