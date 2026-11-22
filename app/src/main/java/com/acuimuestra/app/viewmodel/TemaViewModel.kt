package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.repository.ModoTema
import com.acuimuestra.app.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemaViewModel(private val preferencias: PreferenciasRepository) : ViewModel() {

    val modo: StateFlow<ModoTema> =
        preferencias.modoTema.stateIn(viewModelScope, SharingStarted.Eagerly, ModoTema.SISTEMA)

    fun cambiarModo() {
        viewModelScope.launch { preferencias.guardarModoTema(modo.value.siguiente()) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TemaViewModel(contenedor().preferenciasRepository) }
        }
    }
}
