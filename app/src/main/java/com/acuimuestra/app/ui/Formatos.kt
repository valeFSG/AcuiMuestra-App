package com.acuimuestra.app.ui

import com.acuimuestra.app.data.model.EstadoRevision
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFechaHora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

fun Long.comoFechaHora(): String = formatoFechaHora.format(Date(this))

fun EstadoRevision.etiqueta(): String = when (this) {
    EstadoRevision.PENDIENTE -> "Pendiente"
    EstadoRevision.OBSERVADO -> "Observado"
    EstadoRevision.CORREGIDO -> "Corregido"
    EstadoRevision.VALIDADO -> "Validado"
}