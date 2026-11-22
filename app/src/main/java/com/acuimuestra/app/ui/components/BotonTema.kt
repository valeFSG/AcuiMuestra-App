package com.acuimuestra.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.repository.ModoTema
import com.acuimuestra.app.viewmodel.TemaViewModel

@Composable
fun BotonTema(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    viewModel: TemaViewModel = viewModel(factory = TemaViewModel.Factory)
) {
    val modo by viewModel.modo.collectAsStateWithLifecycle()
    IconButton(onClick = viewModel::cambiarModo, modifier = modifier) {
        AnimatedContent(
            targetState = modo,
            transitionSpec = { (scaleIn(tween(200)) + fadeIn(tween(200))) togetherWith fadeOut(tween(120)) },
            label = "modoTema"
        ) { actual ->
            Icon(
                imageVector = when (actual) {
                    ModoTema.SISTEMA -> Icons.Outlined.BrightnessAuto
                    ModoTema.CLARO -> Icons.Outlined.LightMode
                    ModoTema.OSCURO -> Icons.Outlined.DarkMode
                },
                contentDescription = when (actual) {
                    ModoTema.SISTEMA -> "Tema automático. Toca para usar el modo claro"
                    ModoTema.CLARO -> "Modo claro. Toca para usar el modo oscuro"
                    ModoTema.OSCURO -> "Modo oscuro. Toca para usar el tema automático"
                },
                tint = color
            )
        }
    }
}
