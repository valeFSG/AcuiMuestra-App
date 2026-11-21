package com.acuimuestra.app.viewmodel

import com.acuimuestra.app.Datos
import com.acuimuestra.app.MainDispatcherRule
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.Linea
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.Tren
import com.acuimuestra.app.data.model.Usuario
import com.acuimuestra.app.data.remote.HoraRegistro
import com.acuimuestra.app.data.remote.RelojRed
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val muestraRepository = mockk<MuestraRepository>()
    private val catalogo = mockk<CatalogoRepository>()
    private val usuarios = mockk<UsuarioRepository>()
    private val reloj = mockk<RelojRed>()
    private val sesion = MutableStateFlow<Usuario?>(Datos.operador)
    private lateinit var viewModel: RegistroViewModel

    private val formulario get() = viewModel.estado.value.formulario

    @Before
    fun preparar() {
        every { catalogo.centros() } returns flowOf(listOf(Centro(1, "Centro Ficticio Norte"), Centro(2, "Centro Ficticio Sur")))
        every { catalogo.trenes(1) } returns flowOf(listOf(Tren(1, 1, "Tren 1")))
        every { catalogo.trenes(2) } returns flowOf(listOf(Tren(3, 2, "Tren 1")))
        every { catalogo.lineas(any()) } returns flowOf(listOf(Linea(1, 1, "Línea 1")))
        every { usuarios.usuarioActual } returns sesion
        coEvery { reloj.ahora() } returns HoraRegistro(millis = 5_000, verificada = true)
        coEvery { muestraRepository.guardar(any()) } returns 9L
        viewModel = RegistroViewModel(muestraRepository, catalogo, usuarios, reloj)
    }

    private fun TestScope.observar() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.estado.collect {} }
    }

    private fun completarUbicacion() {
        viewModel.seleccionarCentro(1)
        viewModel.seleccionarTren(1)
        viewModel.seleccionarLinea(1)
        viewModel.onFotoChange("file:///foto.jpg")
    }

    @Test
    fun ubicacionVacia_marcaLosCamposObligatorios() = runTest {
        observar()

        assertFalse(viewModel.validarUbicacion())

        val errores = formulario.errores
        assertEquals("Selecciona un centro", errores[CampoRegistro.CENTRO])
        assertEquals("Selecciona un tren", errores[CampoRegistro.TREN])
        assertEquals("Selecciona una línea", errores[CampoRegistro.LINEA])
    }

    @Test
    fun seleccionEnCascada_cargaTrenesYLineasYLimpiaAlCambiarCentro() = runTest {
        observar()

        completarUbicacion()
        assertEquals(1, viewModel.estado.value.trenes.size)
        assertEquals(1, viewModel.estado.value.lineas.size)
        assertTrue(viewModel.validarUbicacion())

        viewModel.seleccionarCentro(2)

        assertNull(formulario.trenId)
        assertNull(formulario.lineaId)
        assertEquals(3L, viewModel.estado.value.trenes.first().id)
        assertTrue(viewModel.estado.value.lineas.isEmpty())
    }

    @Test
    fun tramo_validaObligatorioYMaximo() = runTest {
        observar()
        completarUbicacion()

        viewModel.onTramoChange("")
        viewModel.validarUbicacion()
        assertEquals("El tramo es obligatorio", formulario.errores[CampoRegistro.TRAMO])

        viewModel.onTramoChange("600")
        assertEquals("Debe ser menor o igual a 500", formulario.errores[CampoRegistro.TRAMO])

        viewModel.onTramoChange("30")
        assertNull(formulario.errores[CampoRegistro.TRAMO])
    }

    @Test
    fun fechaFutura_esInvalida() = runTest {
        observar()
        completarUbicacion()

        viewModel.onFechaHoraChange(System.currentTimeMillis() + 3_600_000)

        assertFalse(viewModel.validarUbicacion())
        assertEquals("La fecha no puede ser futura", formulario.errores[CampoRegistro.FECHA])
    }

    @Test
    fun conteo_seValidaYSeCorrigeEnVivo() = runTest {
        observar()

        viewModel.onConteoChange("0")
        assertNull("Sin intento previo no se muestra error", formulario.errores[CampoRegistro.CONTEO])

        assertFalse(viewModel.validarConteo())
        assertEquals("Debe ser mayor a 0", formulario.errores[CampoRegistro.CONTEO])

        viewModel.onConteoChange("abc")
        assertEquals("Ingresa un número entero", formulario.errores[CampoRegistro.CONTEO])

        viewModel.onConteoChange("200000")
        assertEquals("Debe ser menor o igual a 100000", formulario.errores[CampoRegistro.CONTEO])

        viewModel.onConteoChange("185")
        assertNull(formulario.errores[CampoRegistro.CONTEO])
    }

    @Test
    fun datosOpcionales_validanRangoYAceptanComa() = runTest {
        observar()
        viewModel.onConteoChange("185")
        viewModel.onCalibreChange("200")
        viewModel.onPesoChange("0")
        viewModel.onObservacionesChange("x".repeat(201))

        assertFalse(viewModel.validarConteo())
        assertEquals("Debe ser menor o igual a 150 mm", formulario.errores[CampoRegistro.CALIBRE])
        assertEquals("Ingresa un número válido mayor a 0", formulario.errores[CampoRegistro.PESO])
        assertEquals("Máximo 200 caracteres", formulario.errores[CampoRegistro.OBSERVACIONES])

        viewModel.onCalibreChange("42,5")
        viewModel.onPesoChange("15000")
        viewModel.onObservacionesChange("Línea con buena densidad")

        assertNull(formulario.errores[CampoRegistro.CALIBRE])
        assertEquals("Debe ser menor o igual a 10000 g", formulario.errores[CampoRegistro.PESO])
        assertNull(formulario.errores[CampoRegistro.OBSERVACIONES])
    }

    @Test
    fun guardar_conDatosValidos_guardaConHoraDeRed() = runTest {
        observar()
        completarUbicacion()
        viewModel.onTramoChange("30")
        viewModel.onConteoChange("185")
        viewModel.onObservacionesChange("  Sin daños  ")
        viewModel.onCalibreChange("42,5")
        viewModel.onPesoChange("1200")
        viewModel.agregarMarca(0.2f, 0.3f)
        viewModel.agregarMarca(0.5f, 0.5f)
        viewModel.usarConteoAsistido()
        viewModel.onConteoChange("185")
        val guardada = slot<Muestra>()
        coEvery { muestraRepository.guardar(capture(guardada)) } returns 9L

        viewModel.guardar()

        assertEquals(9L, formulario.muestraGuardadaId)
        assertEquals(5_000L, formulario.fechaGuardada)
        assertTrue(formulario.horaVerificada)
        assertFalse(formulario.guardando)
        with(guardada.captured) {
            assertEquals(185, conteo)
            assertEquals(30, tramoCm)
            assertEquals("Sin daños", observaciones)
            assertEquals(42.5, calibrePromedioMm!!, 0.0)
            assertEquals(1200.0, pesoGramos!!, 0.0)
            assertEquals(Datos.operador.id, operadorId)
            assertEquals(5_000L, fechaHora)
            assertEquals("file:///foto.jpg", fotoUri)
            assertEquals(2, conteoMarcado)
        }
    }

    @Test
    fun guardar_conDatosInvalidos_noGuarda() = runTest {
        observar()

        viewModel.guardar()

        coVerify(exactly = 0) { muestraRepository.guardar(any()) }
        assertTrue(formulario.errores.isNotEmpty())
    }

    @Test
    fun guardar_sinSesion_muestraError() = runTest {
        observar()
        sesion.value = null
        completarUbicacion()
        viewModel.onConteoChange("185")

        viewModel.guardar()

        assertEquals("Faltan datos o la sesión expiró", formulario.errorGeneral)
        coVerify(exactly = 0) { muestraRepository.guardar(any()) }
    }

    @Test
    fun guardar_siFallaLaBase_muestraError() = runTest {
        observar()
        completarUbicacion()
        viewModel.onConteoChange("185")
        coEvery { muestraRepository.guardar(any()) } throws IllegalStateException("disco lleno")

        viewModel.guardar()

        assertEquals("No se pudo guardar la muestra", formulario.errorGeneral)
        assertFalse(formulario.guardando)
    }

    @Test
    fun reiniciar_dejaElFormularioVacio() = runTest {
        observar()
        completarUbicacion()
        viewModel.onFotoChange("file:///foto.jpg")

        viewModel.reiniciar()

        assertNull(formulario.centroId)
        assertNull(formulario.fotoUri)
    }

    @Test
    fun fotoObligatoria_noDejaAvanzarSinFoto() = runTest {
        observar()

        assertFalse(viewModel.validarFoto())
        assertEquals("Falta la fotografía de la muestra", formulario.errorGeneral)

        viewModel.onFotoChange("file:///foto.jpg")
        assertNull(formulario.errorGeneral)
        assertTrue(viewModel.validarFoto())
    }

    @Test
    fun conteoAsistido_marcaDeshaceYUsaElTotal() = runTest {
        observar()
        viewModel.onFotoChange("file:///foto.jpg")

        viewModel.usarConteoAsistido()
        assertEquals("", formulario.conteo)

        viewModel.agregarMarca(0.1f, 0.1f)
        viewModel.agregarMarca(1.5f, -0.2f)
        viewModel.agregarMarca(0.4f, 0.6f)
        assertEquals(Marca(1f, 0f), formulario.marcas[1])

        viewModel.deshacerMarca()
        assertEquals(2, formulario.marcas.size)

        viewModel.usarConteoAsistido()
        assertEquals("2", formulario.conteo)
        assertEquals(2, formulario.conteoMarcado)

        viewModel.limpiarMarcas()
        assertTrue(formulario.marcas.isEmpty())
    }

    @Test
    fun cambiarLaFoto_borraLasMarcas() = runTest {
        observar()
        viewModel.onFotoChange("file:///foto.jpg")
        viewModel.agregarMarca(0.5f, 0.5f)
        viewModel.usarConteoAsistido()

        viewModel.onFotoChange("file:///foto.jpg")
        assertEquals(1, formulario.marcas.size)

        viewModel.onFotoChange("file:///otra.jpg")
        assertTrue(formulario.marcas.isEmpty())
        assertNull(formulario.conteoMarcado)
    }

    @Test
    fun reglas_conviertenDecimalesConComa() {
        assertEquals(12.5, ReglasRegistro.aDecimal("12,5")!!, 0.0)
        assertNull(ReglasRegistro.aDecimal(""))
        assertNull(ReglasRegistro.decimalOpcional("", 10.0, "g"))
    }

    @Test
    fun densidad_esConteoPorMetroDeTramo() {
        assertEquals(616.67, ReglasRegistro.densidadPorMetro(185, 30)!!, 0.01)
        assertEquals(500.0, ReglasRegistro.densidadPorMetro(300, 60)!!, 0.0)
        assertNull(ReglasRegistro.densidadPorMetro(185, 0))
        assertNull(ReglasRegistro.densidadPorMetro(null, 30))
        assertNull(ReglasRegistro.densidadPorMetro(0, 30))
    }
}
