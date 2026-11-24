package com.acuimuestra.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CodigoMuestraTest {

    @Test
    fun uuid_seMuestraComoCodigoCorto() {
        assertEquals("E513-D334", codigoMuestra("e513d334-dc84-4e8d-8767-5a50cef38a27"))
    }

    @Test
    fun identificadorNoHexadecimal_seMuestraEnMayusculas() {
        assertEquals("SEMILLA-1", codigoMuestra("semilla-1"))
        assertEquals("DEMO-0001", codigoMuestra("demo-0001"))
    }
}
