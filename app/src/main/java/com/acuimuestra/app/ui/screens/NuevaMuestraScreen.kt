package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun NuevaMuestraScreen(onSiguiente: () -> Unit) {
    PantallaEnConstruccion("Nueva muestra", "Siguiente" to onSiguiente)
}