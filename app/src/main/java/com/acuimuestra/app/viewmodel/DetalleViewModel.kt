package com.acuimuestra.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.data.model.Rol
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
    val errorConteo: String? = null,
    val errorTramo: String? = null,
    val errorObservaciones: String? = null
)
data class DetalleUiState(
    val cargando: Boolean = true,
    val muestra: MuestraDetalle? = null,
    val rol: Rol? = null,
    val comentario: String = "",
    val errorComentario: String? = null,
    val mensaje: String? = null,
    val correccion: FormularioCorreccion? = null
) {
    private val enRevision: Boolean
        get() = muestra?.estado == EstadoRevision.PENDIENTE || muestra?.estado == EstadoRevision.CORREGIDO

    val puedeRevisar: Boolean get() = rol == Rol.SUPERVISOR && enRevision
    val puedeCorregir: Boolean get() = rol == Rol.OPERADOR && muestra?.estado == EstadoRevision.OBSERVADO
}

private data class FormularioRevision(
    val comentario: String = "",
    val errorComentario: String? = null,
    val mensaje: String? = null
)

class DetalleViewModel(
    private val muestraId: Long,
    private val muestraRepository: MuestraRepository,
    usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val formulario = MutableStateFlow(FormularioRevision())
    private val correccion = MutableStateFlow<FormularioCorreccion?>(null)

    val estado: StateFlow<DetalleUiState> = combine(
        muestraRepository.detalle(muestraId),
        usuarioRepository.usuarioActual,
        formulario,
        correccion
    ) { muestra, usuario, form, corr ->
        DetalleUiState(
            cargando = false,
            muestra = muestra,
            rol = usuario?.rol,
            comentario = form.comentario,
            errorComentario = form.errorComentario,
            mensaje = form.mensaje,
            correccion = corr
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

    fun iniciarCorreccion() {
        val muestra = estado.value.muestra ?: return
        correccion.value = FormularioCorreccion(
            conteo = muestra.conteo.toString(),
            tramo = muestra.tramoCm.toString(),
            observaciones = muestra.observaciones
        )
    }

    fun cancelarCorreccion() {
        correccion.value = null
    }

    fun onConteoChange(valor: String) = correccion.update { actual ->
        actual?.copy(conteo = valor, errorConteo = actual.errorConteo?.let { validarEntero(valor, "El conteo") })
    }

    fun onTramoChange(valor: String) = correccion.update { actual ->
        actual?.copy(tramo = valor, errorTramo = actual.errorTramo?.let { validarEntero(valor, "El tramo") })
    }

    fun onObservacionesChange(valor: String) = correccion.update { actual ->
        actual?.copy(observaciones = valor, errorObservaciones = validarObservaciones(valor))
    }

    fun guardarCorreccion() {
        val actual = correccion.value ?: return
        val errorConteo = validarEntero(actual.conteo, "El conteo")
        val errorTramo = validarEntero(actual.tramo, "El tramo")
        val errorObs = validarObservaciones(actual.observaciones)
        if (errorConteo != null || errorTramo != null || errorObs != null) {
            correccion.value = actual.copy(errorConteo = errorConteo, errorTramo = errorTramo, errorObservaciones = errorObs)
            return
        }
        viewModelScope.launch {
            muestraRepository.corregir(
                muestraId,
                actual.conteo.trim().toInt(),
                actual.tramo.trim().toInt(),
                actual.observaciones.trim()
            )
            correccion.value = null
            formulario.value = FormularioRevision(mensaje = "Corrección guardada. La muestra vuelve a revisión.")
        }
    }

    fun comentarioEsValido(): Boolean{
        val error = validarComentario(formulario.value.comentario)
        formulario.update { it.copy(errorComentario = error) }
        return  error == null
    }

    fun descartar() {
        if (!comentarioEsValido()) return
        cambiarEstado(EstadoRevision.DESCARTADO, formulario.value.comentario.trim(), "Muestra descartada")
    }

    private fun cambiarEstado(nuevo: EstadoRevision, comentario: String?, mensaje: String) {
        viewModelScope.launch {
            muestraRepository.revisar(muestraId, nuevo, comentario)
            formulario.value = FormularioRevision(mensaje = mensaje)
        }
    }

    companion object {
        const val MIN_COMENTARIO = 10

        fun validarComentario(valor: String): String? = when {
            valor.isBlank() -> "El comentario es obligatorio"
            valor.trim().length < MIN_COMENTARIO -> "Debe tener al menos $MIN_COMENTARIO caracteres"
            else -> null
        }

        const val MAX_OBSERVACIONES = 200

        fun validarEntero(valor: String, campo: String): String? {
            if (valor.isBlank()) return "$campo es obligatorio"
            val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
            return if (numero <= 0) "Debe ser mayor a 0" else null
        }

        fun validarObservaciones(valor: String): String? =
            if (valor.length > MAX_OBSERVACIONES) "Máximo $MAX_OBSERVACIONES caracteres" else null

        fun factory(muestraId: Long) = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                DetalleViewModel(muestraId, contenedor.muestraRepository, contenedor.usuarioRepository)
            }
        }
    }
}