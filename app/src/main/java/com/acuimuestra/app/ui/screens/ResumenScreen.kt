package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.ui.components.FotoMuestra
import com.acuimuestra.app.viewmodel.RegistroViewModel

@Composable
fun ResumenScreen(
    viewModel: RegistroViewModel,
    onGuardado: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val f = estado.formulario

    val centro = estado.centros.find { it.id == f.centroId }?.nombre ?: "—"
    val tren = estado.trenes.find { it.id == f.trenId }?.nombre ?: "—"
    val linea = estado.lineas.find { it.id == f.lineaId }?.nombre ?: "—"

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver, enabled = !f.guardando, contentPadding = PaddingValues(0.dp)) { Text("‹ Volver") }
        Column {
            Text("Paso 4 de 4", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Resumen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Revisa los datos antes de guardar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth())

        if (f.fotoUri != null) {
            FotoMuestra(fotoUri = f.fotoUri, modifier = Modifier.fillMaxWidth().height(180.dp))
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilaResumen("Centro", centro)
                FilaResumen("Tren · Línea", "$tren · $linea")
                FilaResumen("Fecha y hora", "Se asigna al guardar (hora de red)")
                FilaResumen("Tramo muestreado", "${f.tramoCm.trim()} cm")
                FilaResumen("Conteo", "${f.conteo.trim()} individuos")
                if (f.calibrePromedioMm.isNotBlank()) FilaResumen("Calibre promedio", "${f.calibrePromedioMm.trim()} mm")
                if (f.pesoGramos.isNotBlank()) FilaResumen("Peso", "${f.pesoGramos.trim()} g")
                FilaResumen("Fotografía", if (f.fotoUri != null) "Adjunta" else "Sin fotografía")
                FilaResumen("Observaciones", f.observaciones.trim().ifBlank { "Sin observaciones" })
            }
        }

        f.errorGeneral?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = viewModel::guardar,
            enabled = !f.guardando,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (f.guardando) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar muestra", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    f.muestraGuardadaId?.let { id ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Muestra #$id guardada") },
            text = {
                Text(
                    "Fecha y hora: ${f.fechaGuardada?.comoFechaHora() ?: "—"}" +
                            (if (f.horaVerificada) " (hora de red)." else " (sin conexión: hora del dispositivo, no verificada).") +
                            "\nQuedó Pendiente de revisión por el supervisor."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reiniciar()
                    onGuardado()
                }) { Text("Volver al inicio") }
            }
        )
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}