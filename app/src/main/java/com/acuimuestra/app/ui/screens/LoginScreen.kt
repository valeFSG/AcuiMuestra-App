package com.acuimuestra.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun LoginScreen(onLoginExitoso: () -> Unit) {
    PantallaEnConstruccion("Login", "Ingresar" to onLoginExitoso)
}