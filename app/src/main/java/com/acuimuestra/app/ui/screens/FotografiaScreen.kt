package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun FotografiaScreen(onSiguiente: () -> Unit) {
    PantallaEnConstruccion("Fotografía", "Siguiente" to onSiguiente)
}