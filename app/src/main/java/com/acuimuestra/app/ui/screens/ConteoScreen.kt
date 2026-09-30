package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun ConteoScreen(onSiguiente: () -> Unit) {
    PantallaEnConstruccion("Conteo", "Siguiente" to onSiguiente)
}