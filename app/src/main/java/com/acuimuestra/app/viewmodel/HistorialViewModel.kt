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
import kotlinx.coroutines.flow.*

data class HistorialUiState(
    val cargando: Boolean = true,
    val centros: List<Centro> = emptyList(),
    val filtro: FiltroMuestras = FiltroMuestras(),
    val busqueda: String = "",
    val muestras: List<MuestraResumen> = emptyList(),
    val todas: List<MuestraResumen> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModel(
    muestraRepository: MuestraRepository,
    catalogoRepository: CatalogoRepository
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroMuestras())
    private val busqueda = MutableStateFlow("")

    val estado: StateFlow<HistorialUiState> = combine(
        catalogoRepository.centros(),
        filtro,
        filtro.flatMapLatest { muestraRepository.resumen(it) },
        busqueda,
        muestraRepository.resumen()
    ) { centros, filtroActual, muestras, texto, todas ->
        HistorialUiState(
            cargando = false,
            centros = centros,
            filtro = filtroActual,
            busqueda = texto,
            muestras = buscar(muestras, texto),
            todas = todas
        )

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())
    fun filtrarCentro(centroId: Long?) = filtro.update { it.copy(centroId = centroId) }

    fun filtrarEstado(estado: EstadoRevision?) = filtro.update { it.copy(estado = estado) }

    fun onBusquedaChange(texto: String) {
        busqueda.value = texto
    }

    private fun buscar(muestras: List<MuestraResumen>, texto: String): List<MuestraResumen> {
        val t = texto.trim()
        if (t.isEmpty()) return muestras
        return muestras.filter { m ->
            listOf("#${m.id}", m.centroNombre, m.trenNombre, m.lineaNombre, m.operadorNombre, m.conteo.toString())
                .any { it.contains(t, ignoreCase = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                HistorialViewModel(contenedor.muestraRepository, contenedor.catalogoRepository)
            }
        }
    }
}