package com.acuimuestra.app.ui

import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.viewmodel.ReglasRegistro
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFechaHora = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

fun Long.comoFechaHora(): String = formatoFechaHora.format(Date(this))

fun densidadTexto(conteo: Int?, tramoCm: Int?): String =
    ReglasRegistro.densidadPorMetro(conteo, tramoCm)?.let { "${it.roundToInt()} ind/m" } ?: "—"

fun EstadoRevision.etiqueta(): String = when (this) {
    EstadoRevision.PENDIENTE -> "Pendiente"
    EstadoRevision.OBSERVADO -> "Observado"
    EstadoRevision.CORREGIDO -> "Corregido"
    EstadoRevision.VALIDADO -> "Validado"
    EstadoRevision.DESCARTADO -> "Descartado"
}