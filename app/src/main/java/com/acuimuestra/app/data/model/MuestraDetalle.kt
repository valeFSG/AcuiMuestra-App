package com.acuimuestra.app.data.model

data class MuestraDetalle(
    val id: Long,
    val fechaHora: Long,
    val tramoCm: Int,
    val conteo: Int,
    val observaciones: String,
    val fotoUri: String?,
    val calibrePromedioMm: Double?,
    val calibreMinMm: Double?,
    val calibreMaxMm: Double?,
    val pesoGramos: Double?,
    val estado: EstadoRevision,
    val comentarioSupervisor: String?,
    val centroNombre: String,
    val trenNombre: String,
    val lineaNombre: String,
    val operadorNombre: String
)