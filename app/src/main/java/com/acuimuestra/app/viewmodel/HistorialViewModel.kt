package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.FiltroMuestras
import com.acuimuestra.app.data.repository.MuestraRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HistorialUiState(
    val cargando: Boolean = true,
    val centros: List<Centro> = emptyList(),
    val filtro: FiltroMuestras = FiltroMuestras(),
    val muestras: List<MuestraResumen> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModel(
    muestraRepository: MuestraRepository,
    catalogoRepository: CatalogoRepository
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroMuestras())

    val estado: StateFlow<HistorialUiState> = combine(
        catalogoRepository.centros(),
        filtro,
        filtro.flatMapLatest { muestraRepository.resumen(it) }
    ) { centros, filtroActual, muestras ->
        HistorialUiState(cargando = false, centros = centros, filtro = filtroActual, muestras = muestras)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    fun filtrarCentro(centroId: Long?) = filtro.update { it.copy(centroId = centroId) }

    fun filtrarEstado(estado: EstadoRevision?) = filtro.update { it.copy(estado = estado) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                HistorialViewModel(contenedor.muestraRepository, contenedor.catalogoRepository)
            }
        }
    }
}