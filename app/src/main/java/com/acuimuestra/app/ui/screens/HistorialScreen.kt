package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.ui.etiqueta
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.ui.theme.colores
import com.acuimuestra.app.viewmodel.HistorialViewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

@Composable
fun HistorialScreen(
    onVerDetalle: (Long) -> Unit,
    onVolver: () -> Unit,
    viewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp)
        ) {
            TextButton(
                onClick = onVolver,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.offset(x = (-4).dp)
            ) {
                Text("‹ Inicio", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Text("Historial de muestras", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text("${estado.muestras.size} resultado(s)", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(16.dp))
            TextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                placeholder = { Text("Buscar por centro, línea, operador o N°") },
                leadingIcon = { Text("⌕", fontSize = 22.sp) },
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

        Spacer(Modifier.height(12.dp))
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

        when {
            estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.muestras.isEmpty() -> SinResultados()
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.navigationBarsPadding()
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
                simbolo = "‹",
                degradado = Brush.horizontalGradient(listOf(fondo, fondo.copy(alpha = 0f))),
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        if (hayMasDerecha) {
            IndicadorMas(
                simbolo = "›",
                degradado = Brush.horizontalGradient(listOf(fondo.copy(alpha = 0f), fondo, fondo)),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun IndicadorMas(simbolo: String, degradado: Brush, modifier: Modifier) {
    Box(
        modifier.width(64.dp).fillMaxHeight().background(degradado),
        contentAlignment = if (simbolo == "›") Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            simbolo,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun SinResultados() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("⌕", fontSize = 48.sp, color = MaterialTheme.colorScheme.primary)
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
                    Text("Muestra #${muestra.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    EtiquetaEstado(muestra.estado)
                }
                Text("${muestra.centroNombre} · ${muestra.trenNombre} · ${muestra.lineaNombre}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${muestra.fechaHora.comoFechaHora()} · ${muestra.operadorNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${muestra.conteo} individuos",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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