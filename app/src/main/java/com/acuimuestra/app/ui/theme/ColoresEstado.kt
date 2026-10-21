package com.acuimuestra.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.acuimuestra.app.data.model.EstadoRevision

data class ColorEstado(val fondo: Color, val texto: Color)

fun EstadoRevision.colores(): ColorEstado = when (this) {
    EstadoRevision.PENDIENTE -> ColorEstado(SuperficieVariante, TextoSecundario)
    EstadoRevision.OBSERVADO -> ColorEstado(AmbarContenedor, AmbarOscuro)
    EstadoRevision.CORREGIDO -> ColorEstado(AzulContenedor, AzulProfundo)
    EstadoRevision.VALIDADO -> ColorEstado(Color(0xFFD4F2DC), Color(0xFF1B5E20))
    EstadoRevision.DESCARTADO -> ColorEstado(RojoContenedor, RojoOscuro)
}