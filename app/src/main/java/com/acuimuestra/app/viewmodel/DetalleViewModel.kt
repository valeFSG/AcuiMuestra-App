package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.data.model.Rol
import com.acuimuestra.app.data.remote.AnalisisIa
import com.acuimuestra.app.data.repository.AnalisisIaRepository
import com.acuimuestra.app.data.repository.MuestraRepository
import com.acuimuestra.app.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioCorreccion(
    val conteo: String,
    val tramo: String,
    val observaciones: String,
    val calibre: String,
    val peso: String,
    val fotoUri: String?,
    val errorConteo: String? = null,
    val errorTramo: String? = null,
    val errorObservaciones: String? = null,
    val errorCalibre: String? = null,
    val errorPeso: String? = null
)

sealed interface EstadoAnalisisIa {
    data object Inactivo : EstadoAnalisisIa
    data object Cargando : EstadoAnalisisIa
    data class Listo(val resultado: AnalisisIa) : EstadoAnalisisIa
    data class Error(val mensaje: String) : EstadoAnalisisIa
}

data class DetalleUiState(
    val cargando: Boolean = true,
    val muestra: MuestraDetalle? = null,
    val rol: Rol? = null,
    val comentario: String = "",
    val errorComentario: String? = null,
    val mensaje: String? = null,
    val correccion: FormularioCorreccion? = null,
    val analisis: EstadoAnalisisIa = EstadoAnalisisIa.Inactivo
) {
    private val enRevision: Boolean
        get() = muestra?.estado == EstadoRevision.PENDIENTE || muestra?.estado == EstadoRevision.CORREGIDO

    val puedeRevisar: Boolean get() = rol == Rol.SUPERVISOR && enRevision
    val puedeCorregir: Boolean get() = rol == Rol.OPERADOR && muestra?.estado == EstadoRevision.OBSERVADO
    val puedeAnalizar: Boolean get() = (rol == Rol.SUPERVISOR || rol == Rol.ANALISTA) && muestra?.fotoUri != null
}

private data class FormularioRevision(
    val comentario: String = "",
    val errorComentario: String? = null,
    val mensaje: String? = null
)

class DetalleViewModel(
    private val muestraId: Long,
    private val muestraRepository: MuestraRepository,
    usuarioRepository: UsuarioRepository,
    private val analisisIaRepository: AnalisisIaRepository? = null
) : ViewModel() {

    private val formulario = MutableStateFlow(FormularioRevision())
    private val correccion = MutableStateFlow<FormularioCorreccion?>(null)
    private val analisis = MutableStateFlow<EstadoAnalisisIa>(EstadoAnalisisIa.Inactivo)

    val estado: StateFlow<DetalleUiState> = combine(
        muestraRepository.detalle(muestraId),
        usuarioRepository.usuarioActual,
        formulario,
        correccion,
        analisis
    ) { muestra, usuario, form, corr, ia ->
        DetalleUiState(
            cargando = false,
            muestra = muestra,
            rol = usuario?.rol,
            comentario = form.comentario,
            errorComentario = form.errorComentario,
            mensaje = form.mensaje,
            correccion = corr,
            analisis = ia
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    fun onComentarioChange(valor: String) = formulario.update {
        it.copy(
            comentario = valor,
            errorComentario = it.errorComentario?.let { validarComentario(valor) },
            mensaje = null
        )
    }

    fun observar() {
        val comentario = formulario.value.comentario
        val error = validarComentario(comentario)
        if (error != null) {
            formulario.update { it.copy(errorComentario = error) }
            return
        }
        cambiarEstado(EstadoRevision.OBSERVADO, comentario.trim(), "Muestra marcada como observada")
    }

    fun validar() {
        val comentario = formulario.value.comentario.trim().ifBlank { estado.value.muestra?.comentarioSupervisor }
        cambiarEstado(EstadoRevision.VALIDADO, comentario, "Muestra validada")
    }

    fun comentarioEsValido(): Boolean {
        val error = validarComentario(formulario.value.comentario)
        formulario.update { it.copy(errorComentario = error) }
        return error == null
    }

    fun descartar() {
        if (!comentarioEsValido()) return
        cambiarEstado(EstadoRevision.DESCARTADO, formulario.value.comentario.trim(), "Muestra descartada")
    }

    fun analizarConIa() {
        val uuid = estado.value.muestra?.uuid ?: return
        val repositorio = analisisIaRepository ?: return
        if (analisis.value == EstadoAnalisisIa.Cargando) return
        analisis.value = EstadoAnalisisIa.Cargando
        viewModelScope.launch {
            analisis.value = repositorio.analizar(uuid).fold(
                onSuccess = { EstadoAnalisisIa.Listo(it) },
                onFailure = { EstadoAnalisisIa.Error(it.message ?: "No se pudo analizar la foto.") }
            )
        }
    }

    fun iniciarCorreccion() {
        val m = estado.value.muestra ?: return
        correccion.value = FormularioCorreccion(
            conteo = m.conteo.toString(),
            tramo = m.tramoCm.toString(),
            observaciones = m.observaciones,
            calibre = m.calibrePromedioMm?.toString() ?: "",
            peso = m.pesoGramos?.toString() ?: "",
            fotoUri = m.fotoUri
        )
    }

    fun cancelarCorreccion() {
        correccion.value = null
    }

    fun onConteoChange(valor: String) = correccion.update { c ->
        c?.copy(conteo = valor, errorConteo = c.errorConteo?.let { validarEntero(valor, "El conteo") })
    }

    fun onTramoChange(valor: String) = correccion.update { c ->
        c?.copy(tramo = valor, errorTramo = c.errorTramo?.let { validarEntero(valor, "El tramo") })
    }

    fun onObservacionesChange(valor: String) = correccion.update { c ->
        c?.copy(observaciones = valor, errorObservaciones = validarObservaciones(valor))
    }

    fun onCalibreChange(valor: String) = correccion.update { c ->
        c?.copy(calibre = valor, errorCalibre = c.errorCalibre?.let { validarCalibre(valor) })
    }

    fun onPesoChange(valor: String) = correccion.update { c ->
        c?.copy(peso = valor, errorPeso = c.errorPeso?.let { validarPeso(valor) })
    }

    fun onFotoChange(uri: String?) = correccion.update { it?.copy(fotoUri = uri) }

    fun guardarCorreccion() {
        val c = correccion.value ?: return
        val revisada = c.copy(
            errorConteo = validarEntero(c.conteo, "El conteo"),
            errorTramo = validarEntero(c.tramo, "El tramo"),
            errorObservaciones = validarObservaciones(c.observaciones),
            errorCalibre = validarCalibre(c.calibre),
            errorPeso = validarPeso(c.peso)
        )
        val hayErrores = listOf(
            revisada.errorConteo, revisada.errorTramo, revisada.errorObservaciones,
            revisada.errorCalibre, revisada.errorPeso
        ).any { it != null }
        if (hayErrores) {
            correccion.value = revisada
            return
        }
        viewModelScope.launch {
            muestraRepository.corregir(
                id = muestraId,
                conteo = c.conteo.trim().toInt(),
                tramoCm = c.tramo.trim().toInt(),
                observaciones = c.observaciones.trim(),
                calibre = ReglasRegistro.aDecimal(c.calibre),
                peso = ReglasRegistro.aDecimal(c.peso),
                fotoUri = c.fotoUri
            )
            correccion.value = null
            formulario.value = FormularioRevision(mensaje = "Corrección guardada. La muestra vuelve a revisión.")
        }
    }

    private fun cambiarEstado(nuevo: EstadoRevision, comentario: String?, mensaje: String) {
        viewModelScope.launch {
            muestraRepository.revisar(muestraId, nuevo, comentario)
            formulario.value = FormularioRevision(mensaje = mensaje)
        }
    }

    companion object {
        const val MIN_COMENTARIO = 10
        const val MAX_OBSERVACIONES = 200

        fun validarComentario(valor: String): String? = when {
            valor.isBlank() -> "El comentario es obligatorio"
            valor.trim().length < MIN_COMENTARIO -> "Debe tener al menos $MIN_COMENTARIO caracteres"
            else -> null
        }

        fun validarEntero(valor: String, campo: String): String? {
            if (valor.isBlank()) return "$campo es obligatorio"
            val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
            return if (numero <= 0) "Debe ser mayor a 0" else null
        }

        fun validarObservaciones(valor: String): String? =
            if (valor.length > MAX_OBSERVACIONES) "Máximo $MAX_OBSERVACIONES caracteres" else null

        fun validarCalibre(valor: String): String? =
            ReglasRegistro.decimalOpcional(valor, ReglasRegistro.MAX_CALIBRE_MM, "mm")

        fun validarPeso(valor: String): String? =
            ReglasRegistro.decimalOpcional(valor, ReglasRegistro.MAX_PESO_G, "g")

        fun factory(muestraId: Long) = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                DetalleViewModel(
                    muestraId,
                    contenedor.muestraRepository,
                    contenedor.usuarioRepository,
                    contenedor.analisisIaRepository
                )
            }
        }
    }
}