package com.acuimuestra.app.ui

import com.acuimuestra.app.Datos
import com.acuimuestra.app.data.model.EstadoRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportadorPdfTest {

    @Test
    fun resumen_incluyeSoloValidadasOrdenadasPorFecha() {
        val muestras = listOf(
            Datos.resumen(3, EstadoRevision.VALIDADO),
            Datos.resumen(2, EstadoRevision.PENDIENTE),
            Datos.resumen(1, EstadoRevision.VALIDADO),
            Datos.resumen(4, EstadoRevision.CORREGIDO)
        )

        val resumen = ExportadorPdf.resumen(muestras)

        assertEquals(listOf(1L, 3L), resumen.muestras.map { it.id })
        assertEquals(204, resumen.totalIndividuos)
        assertEquals(102, resumen.promedioConteo)
    }

    @Test
    fun resumen_sinValidadas_quedaEnCero() {
        val resumen = ExportadorPdf.resumen(listOf(Datos.resumen(1, EstadoRevision.PENDIENTE)))

        assertTrue(resumen.muestras.isEmpty())
        assertEquals(0, resumen.totalIndividuos)
        assertEquals(0, resumen.promedioConteo)
    }
}