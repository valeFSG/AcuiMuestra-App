package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.repository.ModoTema
import com.acuimuestra.app.data.repository.PreferenciasRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TemaViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    @Test
    fun cambiarModo_guardaElSiguienteModo() = runTest {
        val preferencias = mockk<PreferenciasRepository>()
        every { preferencias.modoTema } returns MutableStateFlow(ModoTema.CLARO)
        coEvery { preferencias.guardarModoTema(any()) } just runs
        val viewModel = TemaViewModel(preferencias)

        assertEquals(ModoTema.CLARO, viewModel.modo.value)

        viewModel.cambiarModo()

        coVerify { preferencias.guardarModoTema(ModoTema.OSCURO) }
    }
}
