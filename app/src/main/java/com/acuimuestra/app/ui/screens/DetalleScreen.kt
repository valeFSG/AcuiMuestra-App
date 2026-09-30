package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun DetalleScreen(muestraId: Long, onVolver: () -> Unit) {
    PantallaEnConstruccion("Detalle muestra #$muestraId", "Volver" to onVolver)
}