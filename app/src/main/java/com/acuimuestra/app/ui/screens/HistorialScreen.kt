package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun HistorialScreen(onVerDetalle: (Long) -> Unit) {
    PantallaEnConstruccion("Historial", "Ver muestra de prueba" to { onVerDetalle(1L) })
}