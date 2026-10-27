package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.Centro
import com.acuimuestra.app.data.model.Linea
import com.acuimuestra.app.data.model.Muestra
import com.acuimuestra.app.data.model.Tren
import com.acuimuestra.app.data.remote.RelojRed
import com.acuimuestra.app.data.repository.CatalogoRepository
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CampoRegistro { CENTRO, TREN, LINEA, FECHA, TRAMO, CONTEO, OBSERVACIONES, CALIBRE, PESO }

data class FormularioRegistro(
    val centroId: Long? = null,
    val trenId: Long? = null,
    val lineaId: Long? = null,
    val fechaHora: Long = System.currentTimeMillis(),
    val tramoCm: String = "20",
    val conteo: String = "",
    val observaciones: String = "",
    val fotoUri: String? = null,
    val calibrePromedioMm: String = "",
    val pesoGramos: String = "",
    val errores: Map<CampoRegistro, String> = emptyMap(),
    val guardando: Boolean = false,
    val muestraGuardadaId: Long? = null,
    val errorGeneral: String? = null,
    val fechaGuardada: Long? = null,
    val horaVerificada: Boolean = true
)

data class RegistroUiState(
    val formulario: FormularioRegistro = FormularioRegistro(),
    val centros: List<Centro> = emptyList(),
    val trenes: List<Tren> = emptyList(),
    val lineas: List<Linea> = emptyList()
)

object ReglasRegistro {
    const val MAX_OBSERVACIONES = 200
    const val MAX_TRAMO_CM = 500
    const val MAX_CONTEO = 100_000

    const val MAX_PESO_G = 10_000.0

    const val MAX_CALIBRE_MM = 150.0
    const val FOTO_OBLIGATORIA = false

    fun validar(campo: CampoRegistro, f: FormularioRegistro): String? = when (campo) {
        CampoRegistro.CENTRO -> if (f.centroId == null) "Selecciona un centro" else null
        CampoRegistro.TREN -> if (f.trenId == null) "Selecciona un tren" else null
        CampoRegistro.LINEA -> if (f.lineaId == null) "Selecciona una línea" else null
        CampoRegistro.FECHA -> if (f.fechaHora > System.currentTimeMillis() + 60_000) "La fecha no puede ser futura" else null
        CampoRegistro.TRAMO -> entero(f.tramoCm, "El tramo", MAX_TRAMO_CM)
        CampoRegistro.CONTEO -> entero(f.conteo, "El conteo", MAX_CONTEO)
        CampoRegistro.OBSERVACIONES -> if (f.observaciones.length > MAX_OBSERVACIONES) "Máximo $MAX_OBSERVACIONES caracteres" else null
        CampoRegistro.CALIBRE -> decimalOpcional(f.calibrePromedioMm, MAX_CALIBRE_MM, "mm")
        CampoRegistro.PESO -> decimalOpcional(f.pesoGramos, MAX_PESO_G, "g")
    }

    private fun entero(valor: String, campo: String, maximo: Int): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            numero <= 0 -> "Debe ser mayor a 0"
            numero > maximo -> "Debe ser menor o igual a $maximo"
            else -> null
        }
    }

    fun decimalOpcional(valor: String, maximo: Double, unidad: String): String? {
        if (valor.isBlank()) return null
        val numero = valor.trim().replace(',', '.').toDoubleOrNull()
        return when {
            numero == null || numero <= 0 -> "Ingresa un número válido mayor a 0"
            numero > maximo -> "Debe ser menor o igual a ${maximo.toInt()} $unidad"
            else -> null
        }
    }

    fun aDecimal(valor: String): Double? = valor.trim().replace(',', '.').toDoubleOrNull()
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModel(
    private val muestraRepository: MuestraRepository,
    catalogoRepository: CatalogoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val relojRed: RelojRed
) : ViewModel() {

    private val formulario = MutableStateFlow(FormularioRegistro())

    private val trenes = formulario.map { it.centroId }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else catalogoRepository.trenes(id) }

    private val lineas = formulario.map { it.trenId }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else catalogoRepository.lineas(id) }

    val estado: StateFlow<RegistroUiState> = combine(
        formulario, catalogoRepository.centros(), trenes, lineas
    ) { form, centros, listaTrenes, listaLineas ->
        RegistroUiState(form, centros, listaTrenes, listaLineas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RegistroUiState())

    fun seleccionarCentro(id: Long) = formulario.update {
        it.copy(centroId = id, trenId = null, lineaId = null, errores = it.errores - CampoRegistro.CENTRO)
    }

    fun seleccionarTren(id: Long) = formulario.update {
        it.copy(trenId = id, lineaId = null, errores = it.errores - CampoRegistro.TREN)
    }

    fun seleccionarLinea(id: Long) = formulario.update {
        it.copy(lineaId = id, errores = it.errores - CampoRegistro.LINEA)
    }

    fun onFechaHoraChange(millis: Long) = actualizar(CampoRegistro.FECHA) { it.copy(fechaHora = millis) }
    fun onTramoChange(valor: String) = actualizar(CampoRegistro.TRAMO) { it.copy(tramoCm = valor) }
    fun onConteoChange(valor: String) = actualizar(CampoRegistro.CONTEO) { it.copy(conteo = valor) }
    fun onObservacionesChange(valor: String) = actualizar(CampoRegistro.OBSERVACIONES) { it.copy(observaciones = valor) }
    fun onCalibreChange(valor: String) = actualizar(CampoRegistro.CALIBRE) { it.copy(calibrePromedioMm = valor) }
    fun onPesoChange(valor: String) = actualizar(CampoRegistro.PESO) { it.copy(pesoGramos = valor) }
    fun onFotoChange(uri: String?) = formulario.update { it.copy(fotoUri = uri, errorGeneral = null) }

    fun validarDatos(): Boolean = validarCampos(CampoRegistro.entries)

    fun validarUbicacion(): Boolean = validarCampos(
        listOf(CampoRegistro.CENTRO, CampoRegistro.TREN, CampoRegistro.LINEA, CampoRegistro.FECHA, CampoRegistro.TRAMO)
    )

    fun validarConteo(): Boolean = validarCampos(
        listOf(CampoRegistro.CONTEO, CampoRegistro.OBSERVACIONES, CampoRegistro.CALIBRE, CampoRegistro.PESO)
    )

    fun guardar() {
        if (!validarDatos()) return
        val f = formulario.value
        if (ReglasRegistro.FOTO_OBLIGATORIA && f.fotoUri == null) {
            formulario.update { it.copy(errorGeneral = "Falta la fotografía de la muestra") }
            return
        }
        val operador = usuarioRepository.usuarioActual.value
        val centroId = f.centroId
        val trenId = f.trenId
        val lineaId = f.lineaId
        if (operador == null || centroId == null || trenId == null || lineaId == null) {
            formulario.update { it.copy(errorGeneral = "Faltan datos o la sesión expiró") }
            return
        }
        viewModelScope.launch {
            formulario.update { it.copy(guardando = true, errorGeneral = null) }
            val hora = relojRed.ahora()
            runCatching {
                muestraRepository.guardar(
                    Muestra(
                        centroId = centroId,
                        trenId = trenId,
                        lineaId = lineaId,
                        operadorId = operador.id,
                        fechaHora = hora.millis,
                        horaVerificada = hora.verificada,
                        tramoCm = f.tramoCm.trim().toInt(),
                        conteo = f.conteo.trim().toInt(),
                        observaciones = f.observaciones.trim(),
                        fotoUri = f.fotoUri,
                        calibrePromedioMm = ReglasRegistro.aDecimal(f.calibrePromedioMm),
                        pesoGramos = ReglasRegistro.aDecimal(f.pesoGramos)
                    )
                )
            }.onSuccess { id ->
                formulario.update {
                    it.copy(
                        guardando = false,
                        muestraGuardadaId = id,
                        fechaGuardada = hora.millis,
                        horaVerificada = hora.verificada
                    )
                }
            }.onFailure {
                formulario.update { it.copy(guardando = false, errorGeneral = "No se pudo guardar la muestra") }
            }
        }
    }

    fun reiniciar() {
        formulario.value = FormularioRegistro()
    }

    private fun actualizar(campo: CampoRegistro, cambio: (FormularioRegistro) -> FormularioRegistro) =
        formulario.update { actual ->
            val nuevo = cambio(actual)
            if (campo !in actual.errores) nuevo
            else {
                val error = ReglasRegistro.validar(campo, nuevo)
                nuevo.copy(errores = if (error == null) nuevo.errores - campo else nuevo.errores + (campo to error))
            }
        }

    private fun validarCampos(campos: List<CampoRegistro>): Boolean {
        val f = formulario.value
        val errores = campos.mapNotNull { campo -> ReglasRegistro.validar(campo, f)?.let { campo to it } }.toMap()
        formulario.update { it.copy(errores = errores) }
        return errores.isEmpty()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                RegistroViewModel(contenedor.muestraRepository, contenedor.catalogoRepository, contenedor.usuarioRepository, contenedor.relojRed)
            }
        }
    }
}