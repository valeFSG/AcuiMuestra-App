package com.acuimuestra.app.data.model

import androidx.room.Embedded

data class MuestraPendiente(
    @Embedded val muestra: Muestra,
    val centroNombre: String,
    val trenNombre: String,
    val lineaNombre: String,
    val operadorUsuario: String
)
