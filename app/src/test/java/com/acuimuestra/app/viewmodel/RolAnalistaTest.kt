package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.Rol
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolAnalistaTest {

    @Test
    fun analista_noPuedeRevisarNiCorregir() {
        EstadoRevision.entries.forEach { estado ->
            val estadoUi = DetalleUiState(cargando = false, muestra = Datos.detalle(estado), rol = Rol.ANALISTA)

            assertFalse(estadoUi.puedeRevisar)
            assertFalse(estadoUi.puedeCorregir)
        }
    }

    @Test
    fun supervisorYOperador_mantienenSusPermisos() {
        assertTrue(DetalleUiState(muestra = Datos.detalle(EstadoRevision.PENDIENTE), rol = Rol.SUPERVISOR).puedeRevisar)
        assertTrue(DetalleUiState(muestra = Datos.detalle(EstadoRevision.OBSERVADO), rol = Rol.OPERADOR).puedeCorregir)
    }
}