package com.acuimuestra.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Blanco,
    secondary = VerdeSecundario,
    onSecondary = Blanco,
    tertiary = AmbarAlerta,
    onTertiary = TextoOscuro,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = Blanco,
    onSurface = TextoOscuro,
    error = RojoError,
    onError = Blanco
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulPrincipal,
    onPrimary = Blanco,
    secondary = VerdeSecundario,
    onSecondary = Blanco,
    tertiary = AmbarAlerta,
    onTertiary = TextoOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    error = RojoError,
    onError = Blanco
)

@Composable
fun AcuiMuestraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}