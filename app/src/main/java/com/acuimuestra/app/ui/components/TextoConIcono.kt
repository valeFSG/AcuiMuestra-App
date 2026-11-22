package com.acuimuestra.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TextoConIcono(
    icono: ImageVector,
    texto: String,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    Icon(
        icono,
        contentDescription = null,
        tint = color.takeOrElse { LocalContentColor.current },
        modifier = Modifier.size(18.dp)
    )
    Spacer(Modifier.width(6.dp))
    Text(texto, color = color, fontWeight = fontWeight)
}

@Composable
fun EtiquetaVolver(
    texto: String = "Volver",
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) = TextoConIcono(Icons.AutoMirrored.Outlined.ArrowBack, texto, color, fontWeight)