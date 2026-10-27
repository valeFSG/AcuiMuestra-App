package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acuimuestra.app.viewmodel.CampoRegistro
import com.acuimuestra.app.viewmodel.RegistroViewModel

@Composable
fun NuevaMuestraScreen(
    viewModel: RegistroViewModel,
    onSiguiente: () -> Unit,
    onCancelar: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val f = estado.formulario

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextButton(onClick = onCancelar, contentPadding = PaddingValues(0.dp)) { Text("‹ Cancelar") }
        Column {
            Text("Paso 1 de 4", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Nueva muestra", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "¿Dónde y cuándo se tomó la muestra?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(progress = { 0.25f }, modifier = Modifier.fillMaxWidth())

        Selector(
            titulo = "Centro de cultivo *",
            opciones = estado.centros.map { it.id to it.nombre },
            seleccionado = f.centroId,
            vacio = "Cargando centros...",
            error = f.errores[CampoRegistro.CENTRO],
            onSeleccionar = viewModel::seleccionarCentro
        )
        Selector(
            titulo = "Tren *",
            opciones = estado.trenes.map { it.id to it.nombre },
            seleccionado = f.trenId,
            vacio = "Primero selecciona un centro",
            error = f.errores[CampoRegistro.TREN],
            onSeleccionar = viewModel::seleccionarTren
        )
        Selector(
            titulo = "Línea *",
            opciones = estado.lineas.map { it.id to it.nombre },
            seleccionado = f.lineaId,
            vacio = "Primero selecciona un tren",
            error = f.errores[CampoRegistro.LINEA],
            onSeleccionar = viewModel::seleccionarLinea
        )

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Fecha y hora", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Se registra automáticamente al guardar, usando la hora de red. No se puede modificar.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(
            value = f.tramoCm,
            onValueChange = viewModel::onTramoChange,
            label = { Text("Tramo muestreado (cm) *") },
            singleLine = true,
            isError = f.errores[CampoRegistro.TRAMO] != null,
            supportingText = f.errores[CampoRegistro.TRAMO]?.let { mensaje -> { Text(mensaje) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { if (viewModel.validarUbicacion()) onSiguiente() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Siguiente: conteo", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Selector(
    titulo: String,
    opciones: List<Pair<Long, String>>,
    seleccionado: Long?,
    vacio: String,
    error: String?,
    onSeleccionar: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.titleSmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        if (opciones.isEmpty()) {
            Text(vacio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(opciones) { (id, nombre) ->
                    FilterChip(
                        selected = id == seleccionado,
                        onClick = { onSeleccionar(id) },
                        label = { Text(nombre) },
                        shape = CircleShape
                    )
                }
            }
        }
        error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
    }
}