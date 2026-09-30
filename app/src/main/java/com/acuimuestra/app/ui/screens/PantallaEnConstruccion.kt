package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaEnConstruccion(titulo: String, vararg acciones: Pair<String, () -> Unit>) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        Text("Pantalla en construcción", style = MaterialTheme.typography.bodyMedium)
        acciones.forEach { (texto, accion) ->
            Button(onClick = accion, modifier = Modifier.fillMaxWidth()) { Text(texto) }
        }
    }
}