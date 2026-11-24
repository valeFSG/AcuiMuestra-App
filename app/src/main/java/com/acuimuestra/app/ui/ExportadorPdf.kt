package com.acuimuestra.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.acuimuestra.app.data.model.codigoMuestra
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraResumen
import java.io.File

data class ResumenPdf(
    val muestras: List<MuestraResumen>,
    val totalIndividuos: Int,
    val promedioConteo: Int
)

object ExportadorPdf {
    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN = 40f
    private const val ALTO_ENCABEZADO = 22f
    private const val ALTO_FILA = 36f
    private const val INICIO_TABLA = 108f

    private val AZUL = 0xFF0277BD.toInt()
    private val AZUL_PROFUNDO = 0xFF01344F.toInt()
    private val TEXTO = 0xFF1B1F23.toInt()
    private val GRIS = 0xFF41505C.toInt()
    private val FILA_ALTERNA = 0xFFEAF2F8.toInt()
    private val BLANCO = 0xFFFFFFFF.toInt()

    private val columnas = listOf(
        "Código" to 45f,
        "Fecha y hora" to 82f,
        "Centro" to 90f,
        "Tren" to 45f,
        "Línea" to 50f,
        "Tramo" to 40f,
        "Conteo" to 42f,
        "Densidad" to 58f,
        "Estado" to 63f
    )

    fun resumen(muestras: List<MuestraResumen>): ResumenPdf {
        val validadas = muestras.filter { it.estado == EstadoRevision.VALIDADO }.sortedBy { it.fechaHora }
        val total = validadas.sumOf { it.conteo }
        return ResumenPdf(validadas, total, if (validadas.isEmpty()) 0 else total / validadas.size)
    }

    fun generar(context: Context, muestras: List<MuestraResumen>): File {
        val datos = resumen(muestras)
        val titulo = pincel(20f, AZUL_PROFUNDO, negrita = true)
        val subtitulo = pincel(10f, GRIS)
        val encabezado = pincel(8.5f, BLANCO, negrita = true)
        val texto = pincel(8.5f, TEXTO)
        val nota = pincel(7.5f, GRIS)
        val fondoEncabezado = Paint().apply { color = AZUL }
        val fondoAlterno = Paint().apply { color = FILA_ALTERNA }

        val filasPorPagina = ((ALTO - MARGEN - INICIO_TABLA - ALTO_ENCABEZADO) / ALTO_FILA).toInt()
        val paginas = datos.muestras.chunked(filasPorPagina).ifEmpty { listOf(emptyList()) }
        val documento = PdfDocument()

        paginas.forEachIndexed { indice, filas ->
            val pagina = documento.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, indice + 1).create())
            val lienzo = pagina.canvas
            lienzo.drawText("AcuiMuestra · Reporte de muestras validadas", MARGEN, MARGEN + 20f, titulo)
            lienzo.drawText(
                "Generado: ${System.currentTimeMillis().comoFechaHora()}  ·  ${datos.muestras.size} muestra(s)  ·  " +
                        "${datos.totalIndividuos} individuos  ·  promedio ${datos.promedioConteo} ind/muestra",
                MARGEN, MARGEN + 38f, subtitulo
            )

            var y = INICIO_TABLA
            lienzo.drawRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_ENCABEZADO, fondoEncabezado)
            dibujarFila(lienzo, columnas.map { it.first }, y, encabezado)
            y += ALTO_ENCABEZADO

            filas.forEachIndexed { n, m ->
                if (n % 2 == 1) lienzo.drawRect(MARGEN, y, ANCHO - MARGEN, y + ALTO_FILA, fondoAlterno)
                dibujarFila(
                    lienzo,
                    listOf(
                        codigoMuestra(m.uuid), m.fechaHora.comoFechaHora(), m.centroNombre, m.trenNombre, m.lineaNombre,
                        "${m.tramoCm} cm", "${m.conteo}", densidadTexto(m.conteo, m.tramoCm), m.estado.etiqueta()
                    ),
                    y, texto
                )
                val observacion = "Observaciones: ${m.observaciones.ifBlank { "Sin observaciones" }}"
                val corta = TextUtils.ellipsize(observacion, nota, ANCHO - 2 * MARGEN - 12f, TextUtils.TruncateAt.END)
                lienzo.drawText(corta.toString(), MARGEN + 6f, y + 29f, nota)
                y += ALTO_FILA
            }
            if (filas.isEmpty()) lienzo.drawText("No hay muestras validadas en la selección.", MARGEN, y + 16f, subtitulo)

            val numero = "Página ${indice + 1} de ${paginas.size}"
            lienzo.drawText(numero, ANCHO - MARGEN - subtitulo.measureText(numero), ALTO - 20f, subtitulo)
            documento.finishPage(pagina)
        }

        val carpeta = File(context.cacheDir, "exportes").apply { mkdirs() }
        val archivo = File(carpeta, "muestras_validadas_${System.currentTimeMillis()}.pdf")
        try {
            archivo.outputStream().use { documento.writeTo(it) }
        } finally {
            documento.close()
        }
        return archivo
    }

    private fun dibujarFila(lienzo: Canvas, valores: List<String>, y: Float, pincel: TextPaint) {
        var x = MARGEN + 6f
        valores.zip(columnas).forEach { (valor, columna) ->
            val corto = TextUtils.ellipsize(valor, pincel, columna.second - 10f, TextUtils.TruncateAt.END)
            lienzo.drawText(corto.toString(), x, y + 15f, pincel)
            x += columna.second
        }
    }

    private fun pincel(tamano: Float, colorTexto: Int, negrita: Boolean = false) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = tamano
            color = colorTexto
            if (negrita) typeface = Typeface.DEFAULT_BOLD
        }
}

fun compartirPdf(context: Context, archivo: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Muestras validadas AcuiMuestra")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar PDF"))
}