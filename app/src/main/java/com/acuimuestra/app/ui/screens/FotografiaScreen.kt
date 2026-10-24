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
import com.acuimuestra.app.ui.components.SelectorFoto
import com.acuimuestra.app.viewmodel.RegistroViewModel
import com.acuimuestra.app.viewmodel.ReglasRegistro

@Composable
fun FotografiaScreen(
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp)) { Text("‹ Volver") }
        Column {
            Text("Paso 3 de 4", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Fotografía", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                if (ReglasRegistro.FOTO_OBLIGATORIA) "Adjunta una foto de la muestra"
                else "Adjunta una foto de la muestra (opcional)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth())

        SelectorFoto(
            fotoUri = f.fotoUri,
            onFotoChange = viewModel::onFotoChange,
            error = f.errorGeneral
        )

        Button(
            onClick = onSiguiente,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (f.fotoUri == null) "Continuar sin foto" else "Siguiente: resumen", fontWeight = FontWeight.SemiBold)
        }
    }
}