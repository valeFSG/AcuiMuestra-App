package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.ui.etiqueta
import com.acuimuestra.app.viewmodel.HistorialViewModel

@Composable
fun HistorialScreen(
    onVerDetalle: (Long) -> Unit,
    viewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)) {
            Text("Historial de muestras", style = MaterialTheme.typography.headlineSmall)
            Text(
                "${estado.muestras.size} resultado(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FilaFiltros(
            opciones = listOf<Pair<Long?, String>>(null to "Todos los centros") +
                    estado.centros.map { it.id to it.nombre },
            seleccionado = estado.filtro.centroId,
            onSeleccionar = viewModel::filtrarCentro
        )
        FilaFiltros(
            opciones = listOf<Pair<EstadoRevision?, String>>(null to "Todos los estados") +
                    EstadoRevision.entries.map { it to it.etiqueta() },
            seleccionado = estado.filtro.estado,
            onSeleccionar = viewModel::filtrarEstado
        )

        when {
            estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.muestras.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No hay muestras con estos filtros",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estado.muestras, key = { it.id }) { muestra ->
                    TarjetaMuestra(muestra, onClick = { onVerDetalle(muestra.id) })
                }
            }
        }
    }
}

@Composable
private fun <T> FilaFiltros(
    opciones: List<Pair<T?, String>>,
    seleccionado: T?,
    onSeleccionar: (T?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { (valor, texto) ->
            FilterChip(
                selected = valor == seleccionado,
                onClick = { onSeleccionar(valor) },
                label = { Text(texto) }
            )
        }
    }
}

@Composable
private fun TarjetaMuestra(muestra: MuestraResumen, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Muestra #${muestra.id}", style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(muestra.estado)
            }
            Text(
                "${muestra.centroNombre} · ${muestra.trenNombre} · ${muestra.lineaNombre}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${muestra.fechaHora.comoFechaHora()} · ${muestra.operadorNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${muestra.conteo} individuos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EtiquetaEstado(estado: EstadoRevision) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when (estado) {
        EstadoRevision.PENDIENTE -> colores.secondaryContainer to colores.onSecondaryContainer
        EstadoRevision.OBSERVADO -> colores.errorContainer to colores.onErrorContainer
        EstadoRevision.CORREGIDO -> colores.tertiaryContainer to colores.onTertiaryContainer
        EstadoRevision.VALIDADO -> colores.primaryContainer to colores.onPrimaryContainer
    }
    Surface(color = fondo, contentColor = texto, shape = MaterialTheme.shapes.small) {
        Text(
            estado.etiqueta(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}