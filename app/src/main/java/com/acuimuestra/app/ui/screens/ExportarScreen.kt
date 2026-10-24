package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.ui.ExportadorCsv
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.ui.compartirCsv
import com.acuimuestra.app.ui.etiqueta
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.viewmodel.ExportarViewModel
import com.acuimuestra.app.viewmodel.Periodo

@Composable
fun ExportarScreen(
    onVolver: () -> Unit,
    viewModel: ExportarViewModel = viewModel(factory = ExportarViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val seleccionadas = estado.seleccionadas

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
        ) {
            TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp)) {
                Text("‹ Historial", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Text("Exportar muestras", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Elige qué muestras incluir en el archivo CSV", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TituloPaso("1", "Filtra", Modifier.padding(horizontal = 16.dp))
                        Chips(
                            opciones = listOf<Pair<Long?, String>>(null to "Todos los centros") + estado.centros.map { it.id to it.nombre },
                            seleccionado = estado.centroId,
                            onElegir = viewModel::elegirCentro
                        )
                        Chips(
                            opciones = listOf<Pair<EstadoRevision?, String>>(null to "Todos los estados") + EstadoRevision.entries.map { it to it.etiqueta() },
                            seleccionado = estado.estado,
                            onElegir = viewModel::elegirEstado
                        )
                        Chips(
                            opciones = Periodo.entries.map { it to it.etiqueta },
                            seleccionado = estado.periodo,
                            onElegir = { it?.let(viewModel::elegirPeriodo) }
                        )
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TituloPaso("2", "Elige las muestras (${seleccionadas.size}/${estado.muestras.size})", Modifier.weight(1f))
                    TextButton(onClick = viewModel::seleccionarTodas) { Text("Todas") }
                    TextButton(onClick = viewModel::seleccionarNinguna) { Text("Ninguna") }
                }
            }

            if (estado.muestras.isEmpty()) {
                item {
                    Text(
                        "No hay muestras con estos filtros",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(estado.muestras, key = { it.id }) { m ->
                FilaSeleccion(
                    muestra = m,
                    marcada = m.id !in estado.excluidas,
                    onAlternar = { viewModel.alternar(m.id) }
                )
            }
        }

        Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
                Text(
                    "Columnas: id, fecha, centro, tren, línea, operador, conteo, estado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { compartirCsv(context, ExportadorCsv.generar(seleccionadas)) },
                    enabled = seleccionadas.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        if (seleccionadas.isEmpty()) "Selecciona al menos una muestra"
                        else "Exportar ${seleccionadas.size} muestra(s) en CSV",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TituloPaso(numero: String, texto: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(numero, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Text(texto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun <T> Chips(opciones: List<Pair<T?, String>>, seleccionado: T?, onElegir: (T?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { (valor, texto) ->
            FilterChip(
                selected = valor == seleccionado,
                onClick = { onElegir(valor) },
                label = { Text(texto) },
                shape = CircleShape
            )
        }
    }
}

@Composable
private fun FilaSeleccion(muestra: MuestraResumen, marcada: Boolean, onAlternar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (marcada) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (marcada) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onAlternar)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = marcada, onCheckedChange = { onAlternar() })
            Column(Modifier.weight(1f)) {
                Text("Muestra #${muestra.id} · ${muestra.conteo} ind.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${muestra.centroNombre} · ${muestra.trenNombre} · ${muestra.lineaNombre}", style = MaterialTheme.typography.bodySmall)
                Text(muestra.fechaHora.comoFechaHora(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            EtiquetaEstado(muestra.estado)
            Spacer(Modifier.width(8.dp))
        }
    }
}