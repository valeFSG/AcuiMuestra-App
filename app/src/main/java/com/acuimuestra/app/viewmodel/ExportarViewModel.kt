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

enum class Periodo(val etiqueta: String, val dias: Int?) {
    TODO("Todo", null),
    HOY("Últimas 24 h", 1),
    SEMANA("Últimos 7 días", 7),
    MES("Últimos 30 días", 30)
}

data class ExportarUiState(
    val centros: List<Centro> = emptyList(),
    val centroId: Long? = null,
    val estado: EstadoRevision? = null,
    val periodo: Periodo = Periodo.TODO,
    val muestras: List<MuestraResumen> = emptyList(),
    val excluidas: Set<Long> = emptySet()
) {
    val seleccionadas: List<MuestraResumen> get() = muestras.filterNot { it.id in excluidas }
}

private data class OpcionesExportar(
    val centroId: Long? = null,
    val estado: EstadoRevision? = null,
    val periodo: Periodo = Periodo.TODO,
    val excluidas: Set<Long> = emptySet()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExportarViewModel(
    muestraRepository: MuestraRepository,
    catalogoRepository: CatalogoRepository
) : ViewModel() {

    private val opciones = MutableStateFlow(OpcionesExportar())

    private val muestras = opciones
        .map { o ->
            FiltroMuestras(
                centroId = o.centroId,
                estado = o.estado,
                desde = o.periodo.dias?.let { System.currentTimeMillis() - it * 86_400_000L }
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { muestraRepository.resumen(it) }

    val estado: StateFlow<ExportarUiState> = combine(
        catalogoRepository.centros(), opciones, muestras
    ) { centros, o, lista ->
        ExportarUiState(centros, o.centroId, o.estado, o.periodo, lista, o.excluidas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExportarUiState())

    fun elegirCentro(id: Long?) = opciones.update { it.copy(centroId = id, excluidas = emptySet()) }
    fun elegirEstado(e: EstadoRevision?) = opciones.update { it.copy(estado = e, excluidas = emptySet()) }
    fun elegirPeriodo(p: Periodo) = opciones.update { it.copy(periodo = p, excluidas = emptySet()) }

    fun alternar(id: Long) = opciones.update {
        it.copy(excluidas = if (id in it.excluidas) it.excluidas - id else it.excluidas + id)
    }

    fun seleccionarTodas() = opciones.update { it.copy(excluidas = emptySet()) }

    fun seleccionarNinguna() = opciones.update { o ->
        o.copy(excluidas = estado.value.muestras.map { it.id }.toSet())
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                ExportarViewModel(c.muestraRepository, c.catalogoRepository)
            }
        }
    }
}