package com.acuimuestra.app.ui.screens

import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.ui.graphics.vector.ImageVector
import com.acuimuestra.app.ui.components.EtiquetaVolver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.data.model.codigoMuestra
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.ui.densidadTexto
import com.acuimuestra.app.ui.etiqueta
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.ui.theme.colores
import com.acuimuestra.app.viewmodel.EstadoSincronizacion
import com.acuimuestra.app.viewmodel.HistorialViewModel

@Composable
fun HistorialScreen(
    onVerDetalle: (Long) -> Unit,
    onVolver: () -> Unit,
    onExportar: () -> Unit,
    onDashboard: () -> Unit,
    viewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sincronizacion by viewModel.sincronizacion.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        item(key = "encabezado") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)
            ) {
                TextButton(
                    onClick = onVolver,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.offset(x = (-4).dp)
                ) {
                    EtiquetaVolver("Inicio", Color.White, FontWeight.SemiBold)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Historial",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${estado.muestras.size} resultado(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    TextButton(onClick = onDashboard, enabled = estado.todas.isNotEmpty()) {
                        Icon(Icons.Outlined.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Panel", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onExportar,
                        enabled = estado.todas.isNotEmpty(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Exportar")
                    }
                }
                Spacer(Modifier.height(12.dp))
                BarraSincronizacion(sincronizacion, onSincronizar = viewModel::sincronizar)
                Spacer(Modifier.height(12.dp))
                TextField(
                    value = estado.busqueda,
                    onValueChange = viewModel::onBusquedaChange,
                    placeholder = { Text("Buscar por centro, línea, operador o código", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = if (estado.busqueda.isNotEmpty()) {
                        { TextButton(onClick = { viewModel.onBusquedaChange("") }) { Text("Limpiar") } }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "filtros") {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                FilaFiltros(
                    opciones = listOf<Pair<Long?, String>>(null to "Todos los centros") + estado.centros.map { it.id to it.nombre },
                    seleccionado = estado.filtro.centroId,
                    onSeleccionar = viewModel::filtrarCentro
                )
                FilaFiltros(
                    opciones = listOf<Pair<EstadoRevision?, String>>(null to "Todos los estados") +
                            EstadoRevision.entries.map { it to it.etiqueta() },
                    seleccionado = estado.filtro.estado,
                    onSeleccionar = viewModel::filtrarEstado
                )
            }
        }

        when {
            estado.cargando -> item(key = "cargando") {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.muestras.isEmpty() -> item(key = "vacio") { SinResultados() }

            else -> items(estado.muestras, key = { it.id }) { muestra ->
                Box(Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
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
    val fondo = MaterialTheme.colorScheme.background
    val estadoLista = rememberLazyListState()
    val hayMasDerecha by remember { derivedStateOf { estadoLista.canScrollForward } }
    val hayMasIzquierda by remember { derivedStateOf { estadoLista.canScrollBackward } }

    Box(Modifier.fillMaxWidth().height(44.dp)) {
        LazyRow(
            state = estadoLista,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            items(opciones) { (valor, texto) ->
                FilterChip(
                    selected = valor == seleccionado,
                    onClick = { onSeleccionar(valor) },
                    label = { Text(texto) },
                    shape = CircleShape
                )
            }
        }

        if (hayMasIzquierda) {
            IndicadorMas(
                icono = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                alineacion = Alignment.CenterStart,
                degradado = Brush.horizontalGradient(listOf(fondo, fondo.copy(alpha = 0f))),
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        if (hayMasDerecha) {
            IndicadorMas(
                icono = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                alineacion = Alignment.CenterEnd,
                degradado = Brush.horizontalGradient(listOf(fondo.copy(alpha = 0f), fondo, fondo)),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun IndicadorMas(icono: ImageVector, degradado: Brush, alineacion: Alignment, modifier: Modifier) {
    Box(
        modifier.width(64.dp).fillMaxHeight().background(degradado),
        contentAlignment = alineacion
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp).size(28.dp)
        )
    }
}

@Composable
private fun SinResultados() {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Sin muestras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "No hay registros con estos filtros. Prueba con otro centro, estado o búsqueda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun TarjetaMuestra(muestra: MuestraResumen, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(muestra.estado.colores().texto))
            Column(Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Muestra ${codigoMuestra(muestra.uuid)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    EtiquetaEstado(muestra.estado)
                }
                Text("${muestra.centroNombre} · ${muestra.trenNombre} · ${muestra.lineaNombre}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${muestra.fechaHora.comoFechaHora()} · ${muestra.operadorNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${muestra.conteo} individuos · ${densidadTexto(muestra.conteo, muestra.tramoCm)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!muestra.sincronizada) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Pendiente de sincronización",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraSincronizacion(estado: EstadoSincronizacion, onSincronizar: () -> Unit) {
    val (icono, texto) = when {
        !estado.enLinea -> Icons.Outlined.CloudOff to "Sin conexión · ${estado.pendientes} pendiente(s)"
        estado.sincronizando -> null to "Sincronizando…"
        estado.pendientes > 0 -> Icons.Outlined.Sync to "${estado.pendientes} pendiente(s) de envío"
        else -> Icons.Outlined.CheckCircle to "Todo sincronizado"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono == null) {
            CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onSincronizar,
            enabled = estado.enLinea && !estado.sincronizando,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Text("Sincronizar", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EtiquetaEstado(estado: EstadoRevision) {
    val colores = estado.colores()
    Surface(color = colores.fondo, contentColor = colores.texto, shape = CircleShape) {
        Text(
            estado.etiqueta(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private enum class PasoExportar { ELEGIR, VISTA_PREVIA }

@Composable
private fun DialogoElegirExportacion(
    filtradas: Int,
    todas: Int,
    exportarTodas: Boolean,
    onCambiar: (Boolean) -> Unit,
    onContinuar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Qué quieres exportar?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OpcionExportar("Lo que estoy viendo", "$filtradas muestra(s) con los filtros actuales", !exportarTodas, filtradas > 0) { onCambiar(false) }
                OpcionExportar("Todas las muestras", "$todas muestra(s) registradas", exportarTodas, todas > 0) { onCambiar(true) }
            }
        },
        confirmButton = {
            TextButton(onClick = onContinuar, enabled = (if (exportarTodas) todas else filtradas) > 0) { Text("Ver vista previa") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun OpcionExportar(titulo: String, detalle: String, seleccionada: Boolean, habilitada: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = seleccionada, enabled = habilitada, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null, enabled = habilitada)
        Column(Modifier.padding(start = 8.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}