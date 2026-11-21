package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.data.model.Usuario
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val muestraRepository = mockk<MuestraRepository>()
    private val usuarios = mockk<UsuarioRepository>()
    private val muestra = MutableStateFlow<MuestraDetalle?>(Datos.detalle())
    private val sesion = MutableStateFlow<Usuario?>(Datos.supervisor)
    private lateinit var viewModel: DetalleViewModel

    private val estado get() = viewModel.estado.value

    @Before
    fun preparar() {
        every { muestraRepository.detalle(7) } returns muestra
        every { usuarios.usuarioActual } returns sesion
        coEvery { muestraRepository.revisar(any(), any(), any()) } just runs
        coEvery { muestraRepository.corregir(any(), any(), any(), any(), any(), any(), any()) } just runs
        viewModel = DetalleViewModel(7, muestraRepository, usuarios)
    }

    private fun TestScope.observar() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.estado.collect {} }
    }

    @Test
    fun supervisor_puedeRevisarUnaMuestraPendiente() = runTest {
        observar()

        assertFalse(estado.cargando)
        assertTrue(estado.puedeRevisar)
        assertFalse(estado.puedeCorregir)

        muestra.value = Datos.detalle(EstadoRevision.VALIDADO)
        assertFalse(estado.puedeRevisar)
    }

    @Test
    fun observar_exigeComentarioDeAlMenos10Caracteres() = runTest {
        observar()

        viewModel.observar()
        assertEquals("El comentario es obligatorio", estado.errorComentario)

        viewModel.onComentarioChange("corto")
        assertEquals("Debe tener al menos 10 caracteres", estado.errorComentario)

        viewModel.onComentarioChange("Repetir el conteo del tramo")
        assertNull(estado.errorComentario)
        coVerify(exactly = 0) { muestraRepository.revisar(any(), any(), any()) }
    }

    @Test
    fun observar_conComentarioValido_cambiaElEstado() = runTest {
        observar()
        viewModel.onComentarioChange("  Repetir el conteo del tramo ")

        viewModel.observar()

        coVerify { muestraRepository.revisar(7, EstadoRevision.OBSERVADO, "Repetir el conteo del tramo") }
        assertEquals("Muestra marcada como observada", estado.mensaje)
        assertEquals("", estado.comentario)
    }

    @Test
    fun validar_sinComentario_conservaElComentarioAnterior() = runTest {
        muestra.value = Datos.detalle(EstadoRevision.CORREGIDO, comentario = "Repetir el conteo")
        observar()

        viewModel.validar()

        coVerify { muestraRepository.revisar(7, EstadoRevision.VALIDADO, "Repetir el conteo") }
        assertEquals("Muestra validada", estado.mensaje)
    }

    @Test
    fun descartar_validaElComentarioAntes() = runTest {
        observar()

        viewModel.descartar()
        assertFalse(viewModel.comentarioEsValido())
        coVerify(exactly = 0) { muestraRepository.revisar(any(), any(), any()) }

        viewModel.onComentarioChange("Muestra duplicada por error")
        viewModel.descartar()

        coVerify { muestraRepository.revisar(7, EstadoRevision.DESCARTADO, "Muestra duplicada por error") }
        assertEquals("Muestra descartada", estado.mensaje)
    }

    @Test
    fun operador_corrigeUnaMuestraObservada() = runTest {
        sesion.value = Datos.operador
        muestra.value = Datos.detalle(EstadoRevision.OBSERVADO, comentario = "Repetir el conteo")
        observar()
        assertTrue(estado.puedeCorregir)

        viewModel.iniciarCorreccion()
        val inicial = estado.correccion
        assertNotNull(inicial)
        assertEquals("185", inicial!!.conteo)
        assertEquals("42.5", inicial.calibre)

        viewModel.onConteoChange("190")
        viewModel.onTramoChange("35")
        viewModel.onObservacionesChange("Conteo repetido")
        viewModel.onCalibreChange("40")
        viewModel.onPesoChange("1250")
        viewModel.onFotoChange("file:///nueva.jpg")
        viewModel.guardarCorreccion()

        coVerify {
            muestraRepository.corregir(7, 190, 35, "Conteo repetido", 40.0, 1250.0, "file:///nueva.jpg")
        }
        assertNull(estado.correccion)
        assertEquals("Corrección guardada. La muestra vuelve a revisión.", estado.mensaje)
    }

    @Test
    fun correccion_conErrores_noSeGuardaYSeCorrigeEnVivo() = runTest {
        sesion.value = Datos.operador
        muestra.value = Datos.detalle(EstadoRevision.OBSERVADO)
        observar()
        viewModel.iniciarCorreccion()

        viewModel.onConteoChange("0")
        viewModel.onTramoChange("")
        viewModel.onCalibreChange("200")
        viewModel.onPesoChange("abc")
        viewModel.onObservacionesChange("x".repeat(201))
        viewModel.guardarCorreccion()

        val c = estado.correccion!!
        assertEquals("Debe ser mayor a 0", c.errorConteo)
        assertEquals("El tramo es obligatorio", c.errorTramo)
        assertEquals("Debe ser menor o igual a 150 mm", c.errorCalibre)
        assertEquals("Ingresa un número válido mayor a 0", c.errorPeso)
        assertEquals("Máximo 200 caracteres", c.errorObservaciones)
        coVerify(exactly = 0) { muestraRepository.corregir(any(), any(), any(), any(), any(), any(), any()) }

        viewModel.onConteoChange("150")
        viewModel.onTramoChange("30")
        viewModel.onCalibreChange("")
        viewModel.onPesoChange("")
        viewModel.onObservacionesChange("ok")
        val corregida = estado.correccion!!
        assertNull(corregida.errorConteo)
        assertNull(corregida.errorTramo)
        assertNull(corregida.errorCalibre)
        assertNull(corregida.errorPeso)
        assertNull(corregida.errorObservaciones)

        viewModel.cancelarCorreccion()
        assertNull(estado.correccion)
    }

    @Test
    fun sinMuestra_noIniciaCorreccionNiGuarda() = runTest {
        muestra.value = null
        observar()

        viewModel.iniciarCorreccion()
        viewModel.guardarCorreccion()

        assertNull(estado.correccion)
        assertFalse(estado.puedeRevisar)
    }

    @Test
    fun validadores_compartidos() {
        assertEquals("Ingresa un número entero", DetalleViewModel.validarEntero("1,5", "El conteo"))
        assertNull(DetalleViewModel.validarEntero("12", "El conteo"))
        assertNull(DetalleViewModel.validarComentario("Comentario suficiente"))
    }
}
