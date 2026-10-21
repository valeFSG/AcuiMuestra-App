package com.acuimuestra.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrincipal, onPrimary = Blanco,
    primaryContainer = AzulContenedor, onPrimaryContainer = AzulProfundo,
    secondary = VerdeSecundario, onSecondary = Blanco,
    secondaryContainer = VerdeContenedor, onSecondaryContainer = VerdeOscuro,
    tertiary = AmbarAlerta, onTertiary = TextoOscuro,
    tertiaryContainer = AmbarContenedor, onTertiaryContainer = AmbarOscuro,
    error = RojoError, onError = Blanco,
    errorContainer = RojoContenedor, onErrorContainer = RojoOscuro,
    background = FondoClaro, onBackground = TextoOscuro,
    surface = FondoClaro, onSurface = TextoOscuro,
    surfaceVariant = SuperficieVariante, onSurfaceVariant = TextoSecundario,
    surfaceContainerLowest = Blanco, surfaceContainerLow = Blanco,
    surfaceContainer = Blanco, surfaceContainerHigh = Blanco, surfaceContainerHighest = Blanco,
    outline = Borde, outlineVariant = BordeSuave
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color4FB3F0, onPrimary = AzulProfundo,
    primaryContainer = AzulProfundo, onPrimaryContainer = AzulContenedor,
    secondary = Color4DD0C0, onSecondary = VerdeOscuro,
    secondaryContainer = VerdeOscuro, onSecondaryContainer = VerdeContenedor,
    tertiary = AmbarAlerta, onTertiary = TextoOscuro,
    tertiaryContainer = AmbarOscuro, onTertiaryContainer = AmbarContenedor,
    error = ColorFF8A80, onError = RojoOscuro,
    errorContainer = RojoOscuro, onErrorContainer = RojoContenedor,
    background = FondoOscuro, onBackground = TextoClaro,
    surface = FondoOscuro, onSurface = TextoClaro,
    surfaceVariant = SuperficieOscuraAlta, onSurfaceVariant = TextoClaroSecundario,
    surfaceContainerLowest = SuperficieOscura, surfaceContainerLow = SuperficieOscura,
    surfaceContainer = SuperficieOscura, surfaceContainerHigh = SuperficieOscuraAlta,
    surfaceContainerHighest = SuperficieOscuraAlta,
    outline = TextoClaroSecundario, outlineVariant = SuperficieOscuraAlta
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