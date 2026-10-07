package com.acuimuestra.app.data.model

data class MuestraResumen(
    val id: Long,
    val fechaHora: Long,
    val conteo: Int,
    val estado: EstadoRevision,
    val centroNombre: String,
    val trenNombre: String,
    val lineaNombre: String,
    val operadorNombre: String
)