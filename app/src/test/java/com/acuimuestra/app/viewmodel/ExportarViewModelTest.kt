package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.FiltroMuestras
import com.acuimuestra.app.data.repository.MuestraRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportarViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val muestraRepository = mockk<MuestraRepository>()
    private val catalogo = mockk<CatalogoRepository>()
    private val filtros = mutableListOf<FiltroMuestras>()
    private lateinit var viewModel: ExportarViewModel

    private val estado get() = viewModel.estado.value

    @Before
    fun preparar() {
        every { catalogo.centros() } returns flowOf(listOf(Centro(1, "Centro Ficticio Norte")))
        every { muestraRepository.resumen(any()) } answers {
            filtros += firstArg<FiltroMuestras>()
            flowOf(listOf(Datos.resumen(1), Datos.resumen(2), Datos.resumen(3)))
        }
        viewModel = ExportarViewModel(muestraRepository, catalogo)
    }

    private fun TestScope.observar() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.estado.collect {} }
    }

    @Test
    fun porDefecto_seleccionaTodas() = runTest {
        observar()

        assertEquals(3, estado.seleccionadas.size)
        assertNull(filtros.last().desde)
    }

    @Test
    fun alternarYSeleccionarNingunaOTodas() = runTest {
        observar()

        viewModel.alternar(2)
        assertEquals(listOf(1L, 3L), estado.seleccionadas.map { it.id })

        viewModel.alternar(2)
        assertEquals(3, estado.seleccionadas.size)

        viewModel.seleccionarNinguna()
        assertTrue(estado.seleccionadas.isEmpty())

        viewModel.seleccionarTodas()
        assertEquals(3, estado.seleccionadas.size)
    }

    @Test
    fun cambiarFiltros_reiniciaLaSeleccionYConsultaConElPeriodo() = runTest {
        observar()
        viewModel.alternar(1)

        viewModel.elegirCentro(1)
        assertTrue(estado.excluidas.isEmpty())
        assertEquals(1L, filtros.last().centroId)

        viewModel.elegirEstado(EstadoRevision.VALIDADO)
        assertEquals(EstadoRevision.VALIDADO, filtros.last().estado)

        viewModel.elegirPeriodo(Periodo.SEMANA)
        assertEquals(Periodo.SEMANA, estado.periodo)
        assertNotNull(filtros.last().desde)
    }
}
