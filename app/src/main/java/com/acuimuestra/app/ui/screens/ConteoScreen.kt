package com.acuimuestra.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import com.acuimuestra.app.ui.components.EtiquetaVolver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.acuimuestra.app.ui.components.ConteoAsistido
import com.acuimuestra.app.ui.components.FotoMuestra
import com.acuimuestra.app.viewmodel.CampoRegistro
import com.acuimuestra.app.viewmodel.RegistroViewModel
import com.acuimuestra.app.viewmodel.ReglasRegistro

@Composable
fun ConteoScreen(
    viewModel: RegistroViewModel,
    onSiguiente: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val f = estado.formulario
    var mostrarConteo by remember { mutableStateOf(false) }
    val foto = f.fotoUri

    if (mostrarConteo && foto != null) {
        ConteoAsistido(
            fotoUri = foto,
            marcas = f.marcas,
            onAgregar = viewModel::agregarMarca,
            onDeshacer = viewModel::deshacerMarca,
            onLimpiar = viewModel::limpiarMarcas,
            onUsar = {
                viewModel.usarConteoAsistido()
                mostrarConteo = false
            },
            onCerrar = { mostrarConteo = false }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp)) { EtiquetaVolver() }
        Column {
            Text("Paso 3 de 4", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Conteo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Marca los individuos sobre la foto o ingresa el conteo a mano",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth())

        if (foto != null) {
            Card(
                onClick = { mostrarConteo = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    FotoMuestra(fotoUri = foto, modifier = Modifier.size(72.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("Conteo asistido", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (f.marcas.isEmpty()) "Toca para marcar cada chorito sobre la foto"
                            else "${f.marcas.size} marcas · toca para seguir marcando",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        CampoNumero(
            valor = f.conteo,
            onCambio = viewModel::onConteoChange,
            etiqueta = "Conteo de individuos *",
            error = f.errores[CampoRegistro.CONTEO],
            decimal = false
        )
        f.conteoMarcado?.let { marcadas ->
            Text(
                if (f.conteo.trim() == marcadas.toString()) "Conteo asistido: $marcadas marcas sobre la foto"
                else "Conteo asistido: $marcadas marcas · ajustado a mano",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedTextField(
            value = f.observaciones,
            onValueChange = viewModel::onObservacionesChange,
            label = { Text("Observaciones (opcional)") },
            minLines = 3,
            isError = f.errores[CampoRegistro.OBSERVACIONES] != null,
            supportingText = {
                Text(f.errores[CampoRegistro.OBSERVACIONES] ?: "${f.observaciones.length}/${ReglasRegistro.MAX_OBSERVACIONES}")
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Datos opcionales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoNumero(
                valor = f.calibrePromedioMm,
                onCambio = viewModel::onCalibreChange,
                etiqueta = "Calibre promedio (mm)",
                error = f.errores[CampoRegistro.CALIBRE],
                decimal = true,
                modifier = Modifier.weight(1f)
            )
            CampoNumero(
                valor = f.pesoGramos,
                onCambio = viewModel::onPesoChange,
                etiqueta = "Peso total (g)",
                error = f.errores[CampoRegistro.PESO],
                decimal = true,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { if (viewModel.validarConteo()) onSiguiente() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Siguiente: resumen", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    decimal: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { mensaje -> { Text(mensaje) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}