package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun InicioScreen(onNuevaMuestra: () -> Unit, onHistorial: () -> Unit) {
    PantallaEnConstruccion("Inicio", "Nueva muestra" to onNuevaMuestra, "Historial" to onHistorial)
}