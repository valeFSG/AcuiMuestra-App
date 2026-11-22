package com.acuimuestra.app.ui.screens

import com.acuimuestra.app.ui.components.EtiquetaVolver
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.ui.etiqueta
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.ui.theme.colores
import com.acuimuestra.app.viewmodel.BarraDato
import com.acuimuestra.app.viewmodel.DashboardUiState
import com.acuimuestra.app.viewmodel.DashboardViewModel
import com.acuimuestra.app.viewmodel.PuntoEvolucion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onVolver: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
        ) {
            TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp), modifier = Modifier.offset(x = (-4).dp)) {
                EtiquetaVolver("Historial", Color.White, FontWeight.SemiBold)
            }
            Text(
                "Panel de muestras",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Resumen de todas las muestras registradas",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Column(
            Modifier.padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (estado.cargando) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (estado.total == 0) {
                Text("Todavía no hay muestras registradas.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Indicadores(estado)
                SeccionGrafico("Muestras por centro") { GraficoBarras(estado.porCentro, MaterialTheme.colorScheme.primary) }
                SeccionGrafico("Muestras por estado") { GraficoEstados(estado.porEstado) }
                SeccionGrafico("Densidad promedio por centro (ind/m)") {
                    GraficoBarras(estado.densidadPorCentro, MaterialTheme.colorScheme.secondary)
                }
                SeccionGrafico("Evolución del conteo en una línea") {
                    SelectorLinea(estado.lineas, estado.lineaSeleccionada, viewModel::elegirLinea)
                    GraficoEvolucion(estado.evolucion)
                }
            }
        }
    }
}

@Composable
private fun Indicadores(estado: DashboardUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Indicador("Total", estado.total, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        Indicador("Validadas", estado.validadas, EstadoRevision.VALIDADO.colores().texto, Modifier.weight(1f))
        Indicador("Por revisar", estado.porRevisar, EstadoRevision.OBSERVADO.colores().texto, Modifier.weight(1f))
    }
}

@Composable
private fun Indicador(etiqueta: String, valor: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$valor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun SeccionGrafico(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            contenido()
        }
    }
}

@Composable
private fun GraficoBarras(datos: List<BarraDato>, color: Color) {
    val maximo = datos.maxOfOrNull { it.valor }?.coerceAtLeast(1) ?: 1
    datos.forEach { dato -> Barra(dato.etiqueta, dato.valor, maximo, color) }
}

@Composable
private fun GraficoEstados(porEstado: Map<EstadoRevision, Int>) {
    val maximo = porEstado.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    porEstado.forEach { (estado, cantidad) -> Barra(estado.etiqueta(), cantidad, maximo, estado.colores().texto) }
}

@Composable
private fun Barra(etiqueta: String, valor: Int, maximo: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("$valor", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (valor > 0) {
                Box(
                    Modifier
                        .fillMaxWidth(valor / maximo.toFloat())
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun SelectorLinea(lineas: List<String>, seleccionada: String?, onElegir: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(lineas) { linea ->
            FilterChip(
                selected = linea == seleccionada,
                onClick = { onElegir(linea) },
                label = { Text(linea) },
                shape = CircleShape
            )
        }
    }
}

@Composable
private fun GraficoEvolucion(puntos: List<PuntoEvolucion>) {
    if (puntos.isEmpty()) {
        Text(
            "Esta línea no tiene muestras para graficar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val color = MaterialTheme.colorScheme.primary
    val guia = MaterialTheme.colorScheme.outlineVariant
    val maximo = puntos.maxOf { it.conteo }.coerceAtLeast(1)

    Text(
        "Individuos contados por muestra · máximo $maximo",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        repeat(4) { i ->
            val y = size.height * i / 3f
            drawLine(guia, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val posiciones = puntos.mapIndexed { i, punto ->
            val x = if (puntos.size > 1) size.width * i / (puntos.size - 1) else size.width / 2
            val y = size.height - size.height * punto.conteo / maximo
            Offset(x, y)
        }
        posiciones.zipWithNext { desde, hasta ->
            drawLine(color, desde, hasta, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
        posiciones.forEach { drawCircle(color, radius = 5.dp.toPx(), center = it) }
    }
    Row(Modifier.fillMaxWidth()) {
        Text(puntos.first().fechaHora.comoDiaMes(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        if (puntos.size > 1) {
            Text(puntos.last().fechaHora.comoDiaMes(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun Long.comoDiaMes(): String =
    SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL")).format(Date(this))