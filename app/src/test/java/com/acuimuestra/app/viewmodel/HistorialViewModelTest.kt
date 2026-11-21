package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.FiltroMuestras
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.sync.ControlSincronizacion
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val muestraRepository = mockk<MuestraRepository>()
    private val catalogo = mockk<CatalogoRepository>()
    private val control = mockk<ControlSincronizacion>()
    private val enLinea = MutableStateFlow(true)
    private val todas = listOf(
        Datos.resumen(1, EstadoRevision.PENDIENTE, "Centro Ficticio Norte"),
        Datos.resumen(2, EstadoRevision.VALIDADO, "Centro Ficticio Sur"),
        Datos.resumen(3, EstadoRevision.OBSERVADO, "Centro Ficticio Sur")
    )
    private lateinit var viewModel: HistorialViewModel

    @Before
    fun preparar() {
        every { catalogo.centros() } returns flowOf(listOf(Centro(1, "Centro Ficticio Norte"), Centro(2, "Centro Ficticio Sur")))
        every { muestraRepository.resumen(any()) } answers {
            val filtro = firstArg<FiltroMuestras>()
            flowOf(todas.filter { filtro.estado == null || it.estado == filtro.estado })
        }
        every { muestraRepository.pendientes() } returns flowOf(2)
        every { control.enLinea } returns enLinea
        every { control.sincronizando } returns flowOf(false)
        every { control.sincronizar() } just runs
        viewModel = HistorialViewModel(muestraRepository, catalogo, control)
    }

    private fun TestScope.observar() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.estado.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sincronizacion.collect {} }
    }

    @Test
    fun cargaTodasLasMuestrasYLosCentros() = runTest {
        observar()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertEquals(3, estado.muestras.size)
        assertEquals(2, estado.centros.size)
    }

    @Test
    fun busqueda_filtraPorTextoSinImportarMayusculas() = runTest {
        observar()

        viewModel.onBusquedaChange("sur")
        assertEquals(listOf(2L, 3L), viewModel.estado.value.muestras.map { it.id })

        viewModel.onBusquedaChange("#1")
        assertEquals(listOf(1L), viewModel.estado.value.muestras.map { it.id })

        viewModel.onBusquedaChange("  ")
        assertEquals(3, viewModel.estado.value.muestras.size)
    }

    @Test
    fun filtros_seAplicanEnLaConsulta() = runTest {
        observar()

        viewModel.filtrarEstado(EstadoRevision.VALIDADO)
        viewModel.filtrarCentro(2)

        val estado = viewModel.estado.value
        assertEquals(EstadoRevision.VALIDADO, estado.filtro.estado)
        assertEquals(2L, estado.filtro.centroId)
        assertEquals(listOf(2L), estado.muestras.map { it.id })
        assertEquals(3, estado.todas.size)
    }

    @Test
    fun sincronizacion_muestraElEstadoDeLaRedYLosPendientes() = runTest {
        observar()
        assertTrue(viewModel.sincronizacion.value.enLinea)
        assertEquals(2, viewModel.sincronizacion.value.pendientes)

        enLinea.value = false
        assertFalse(viewModel.sincronizacion.value.enLinea)

        viewModel.sincronizar()
        verify { control.sincronizar() }
    }
}
