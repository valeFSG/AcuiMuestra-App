package com.acuimuestra.app.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.data.model.MuestraResumen
import java.io.File

object ExportadorCsv {
    private val encabezado = listOf("id", "fecha_hora", "centro", "tren", "linea", "operador", "conteo", "estado")

    fun generar(muestras: List<MuestraResumen>): String = buildString {
        appendLine(encabezado.joinToString(";"))
        muestras.forEach { m ->
            val fila = listOf(
                m.id, m.fechaHora.comoFechaHora(), m.centroNombre, m.trenNombre,
                m.lineaNombre, m.operadorNombre, m.conteo, m.estado.etiqueta()
            )
            appendLine(fila.joinToString(";") { escapar(it.toString()) })
        }
    }

    private fun escapar(valor: String): String =
        if (valor.any { it == ';' || it == '"' || it == '\n' }) "\"${valor.replace("\"", "\"\"")}\"" else valor
}

fun MuestraDetalle.resumenTexto(): String = buildString {
    appendLine("AcuiMuestra – Muestra #$id (${estado.etiqueta()})")
    appendLine("Centro: $centroNombre")
    appendLine("Tren / Línea: $trenNombre · $lineaNombre")
    appendLine("Fecha y hora: ${fechaHora.comoFechaHora()}")
    appendLine("Operador: $operadorNombre")
    appendLine("Tramo: $tramoCm cm")
    appendLine("Conteo: $conteo individuos")
    calibrePromedioMm?.let { appendLine("Calibre promedio: $it mm") }
    pesoGramos?.let { appendLine("Peso: $it g") }
    appendLine("Observaciones: ${observaciones.ifBlank { "Sin observaciones" }}")
    comentarioSupervisor?.let { appendLine("Comentario del supervisor: $it") }
}

fun compartirTexto(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
}

fun compartirCsv(context: Context, contenido: String) {
    val carpeta = File(context.cacheDir, "exportes").apply { mkdirs() }
    val archivo = File(carpeta, "muestras_${System.currentTimeMillis()}.csv")
    archivo.writeText("\uFEFF$contenido")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Muestras AcuiMuestra")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar muestras"))
}