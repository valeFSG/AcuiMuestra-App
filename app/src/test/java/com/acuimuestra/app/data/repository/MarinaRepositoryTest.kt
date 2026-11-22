package com.acuimuestra.app.data.repository

import com.acuimuestra.app.data.remote.ActualMarina
import com.acuimuestra.app.data.remote.MarinaApi
import com.acuimuestra.app.data.remote.RespuestaMarina
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MarinaRepositoryTest {

    private val api = mockk<MarinaApi>()
    private val repositorio = MarinaRepository(api)

    @Test
    fun conDatos_devuelveLaCondicionYUsaLaUbicacionDeReferencia() = runTest {
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } returns
                RespuestaMarina(ActualMarina("2026-11-22T10:00", 0.8, 11.2))

        val resultado = repositorio.condicionesActuales()

        assertEquals(CondicionMarina(0.8, 11.2, "2026-11-22T10:00"), resultado.getOrNull())
        coVerify {
            api.condicionesActuales(
                MarinaRepository.LATITUD_REFERENCIA, MarinaRepository.LONGITUD_REFERENCIA, any(), any()
            )
        }
    }

    @Test
    fun soloConUnDato_igualDevuelveLaCondicion() = runTest {
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } returns
                RespuestaMarina(ActualMarina(null, null, 10.5))

        assertEquals(10.5, repositorio.condicionesActuales().getOrNull()?.temperaturaMarC)
    }

    @Test
    fun sinBloqueActual_falla() = runTest {
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } returns RespuestaMarina(null)

        assertTrue(repositorio.condicionesActuales().isFailure)
    }

    @Test
    fun sinValores_falla() = runTest {
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } returns
                RespuestaMarina(ActualMarina("2026-11-22T10:00", null, null))

        assertTrue(repositorio.condicionesActuales().isFailure)
    }

    @Test
    fun sinConexion_falla() = runTest {
        coEvery { api.condicionesActuales(any(), any(), any(), any()) } throws IOException("sin red")

        assertTrue(repositorio.condicionesActuales().isFailure)
    }
}