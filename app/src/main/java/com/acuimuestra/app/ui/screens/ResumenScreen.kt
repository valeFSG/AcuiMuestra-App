package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun ResumenScreen(onGuardado: () -> Unit) {
    PantallaEnConstruccion("Resumen", "Guardar" to onGuardado)
}