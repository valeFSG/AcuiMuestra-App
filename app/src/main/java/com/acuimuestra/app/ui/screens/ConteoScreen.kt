package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp)) { Text("‹ Volver") }
        Column {
            Text("Paso 2 de 4", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Conteo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Registra el conteo manual de individuos del tramo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(progress = { 0.5f }, modifier = Modifier.fillMaxWidth())

        CampoNumero(
            valor = f.conteo,
            onCambio = viewModel::onConteoChange,
            etiqueta = "Conteo de individuos *",
            error = f.errores[CampoRegistro.CONTEO],
            decimal = false
        )

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
                etiqueta = "Calibre (mm)",
                error = f.errores[CampoRegistro.CALIBRE],
                decimal = true,
                modifier = Modifier.weight(1f)
            )
            CampoNumero(
                valor = f.pesoGramos,
                onCambio = viewModel::onPesoChange,
                etiqueta = "Peso (g)",
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
            Text("Siguiente: fotografía", fontWeight = FontWeight.SemiBold)
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