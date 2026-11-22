package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.repository.MuestraRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val norte = "Centro Ficticio Norte · Tren 1 · Línea 1"
    private val sur = "Centro Ficticio Sur · Tren 1 · Línea 1"

    private val muestras = listOf(
        Datos.resumen(1, EstadoRevision.PENDIENTE, "Centro Ficticio Norte"),
        Datos.resumen(2, EstadoRevision.VALIDADO, "Centro Ficticio Sur"),
        Datos.resumen(3, EstadoRevision.VALIDADO, "Centro Ficticio Sur"),
        Datos.resumen(4, EstadoRevision.CORREGIDO, "Centro Ficticio Sur"),
        Datos.resumen(5, EstadoRevision.DESCARTADO, "Centro Ficticio Sur")
    )

    @Test
    fun calculaIndicadoresYMuestrasPorCentroYEstado() {
        val estado = calcularDashboard(muestras, null)

        assertFalse(estado.cargando)
        assertEquals(5, estado.total)
        assertEquals(2, estado.validadas)
        assertEquals(2, estado.porRevisar)
        assertEquals(listOf(BarraDato("Centro Ficticio Sur", 4), BarraDato("Centro Ficticio Norte", 1)), estado.porCentro)
        assertEquals(2, estado.porEstado[EstadoRevision.VALIDADO])
        assertEquals(0, estado.porEstado[EstadoRevision.OBSERVADO])
        assertEquals(EstadoRevision.entries.size, estado.porEstado.size)
    }

    @Test
    fun porDefectoEligeLaLineaConMasMuestrasYOrdenaPorFechaSinDescartadas() {
        val estado = calcularDashboard(muestras, null)

        assertEquals(listOf(norte, sur), estado.lineas)
        assertEquals(sur, estado.lineaSeleccionada)
        assertEquals(listOf(2_000L, 3_000L, 4_000L), estado.evolucion.map { it.fechaHora })
        assertEquals(listOf(102, 103, 104), estado.evolucion.map { it.conteo })
    }

    @Test
    fun respetaLaLineaElegidaYIgnoraUnaQueNoExiste() {
        assertEquals(norte, calcularDashboard(muestras, norte).lineaSeleccionada)
        assertEquals(listOf(PuntoEvolucion(1_000L, 101)), calcularDashboard(muestras, norte).evolucion)
        assertEquals(sur, calcularDashboard(muestras, "Línea inexistente").lineaSeleccionada)
    }

    @Test
    fun sinMuestras_quedaVacio() {
        val estado = calcularDashboard(emptyList(), null)

        assertEquals(0, estado.total)
        assertTrue(estado.porCentro.isEmpty())
        assertTrue(estado.lineas.isEmpty())
        assertNull(estado.lineaSeleccionada)
        assertTrue(estado.evolucion.isEmpty())
    }

    @Test
    fun viewModel_observaElRepositorioYCambiaDeLinea() = runTest {
        val repositorio = mockk<MuestraRepository>()
        every { repositorio.resumen(any()) } returns flowOf(muestras)
        val viewModel = DashboardViewModel(repositorio)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.estado.collect {} }

        assertEquals(5, viewModel.estado.value.total)
        assertEquals(sur, viewModel.estado.value.lineaSeleccionada)

        viewModel.elegirLinea(norte)

        assertEquals(norte, viewModel.estado.value.lineaSeleccionada)
        assertEquals(1, viewModel.estado.value.evolucion.size)
    }
}