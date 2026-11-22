package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BarraDato(val etiqueta: String, val valor: Int)

data class PuntoEvolucion(val fechaHora: Long, val conteo: Int)

data class DashboardUiState(
    val cargando: Boolean = true,
    val total: Int = 0,
    val validadas: Int = 0,
    val porRevisar: Int = 0,
    val porCentro: List<BarraDato> = emptyList(),
    val porEstado: Map<EstadoRevision, Int> = emptyMap(),
    val lineas: List<String> = emptyList(),
    val lineaSeleccionada: String? = null,
    val evolucion: List<PuntoEvolucion> = emptyList()
)

class DashboardViewModel(muestraRepository: MuestraRepository) : ViewModel() {

    private val lineaElegida = MutableStateFlow<String?>(null)

    val estado: StateFlow<DashboardUiState> = combine(muestraRepository.resumen(), lineaElegida) { muestras, elegida ->
        calcularDashboard(muestras, elegida)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun elegirLinea(linea: String) {
        lineaElegida.value = linea
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { DashboardViewModel(contenedor().muestraRepository) }
        }
    }
}

fun MuestraResumen.nombreLinea(): String = "$centroNombre · $trenNombre · $lineaNombre"

fun calcularDashboard(muestras: List<MuestraResumen>, elegida: String?): DashboardUiState {
    val lineas = muestras.map { it.nombreLinea() }.distinct().sorted()
    val conMasMuestras = muestras.groupingBy { it.nombreLinea() }.eachCount().maxByOrNull { it.value }?.key
    val seleccionada = elegida?.takeIf { it in lineas } ?: conMasMuestras
    return DashboardUiState(
        cargando = false,
        total = muestras.size,
        validadas = muestras.count { it.estado == EstadoRevision.VALIDADO },
        porRevisar = muestras.count { it.estado == EstadoRevision.PENDIENTE || it.estado == EstadoRevision.CORREGIDO },
        porCentro = muestras.groupingBy { it.centroNombre }.eachCount()
            .map { (centro, cantidad) -> BarraDato(centro, cantidad) }
            .sortedWith(compareByDescending<BarraDato> { it.valor }.thenBy { it.etiqueta }),
        porEstado = EstadoRevision.entries.associateWith { estado -> muestras.count { it.estado == estado } },
        lineas = lineas,
        lineaSeleccionada = seleccionada,
        evolucion = muestras
            .filter { it.nombreLinea() == seleccionada && it.estado != EstadoRevision.DESCARTADO }
            .sortedBy { it.fechaHora }
            .map { PuntoEvolucion(it.fechaHora, it.conteo) }
    )
}