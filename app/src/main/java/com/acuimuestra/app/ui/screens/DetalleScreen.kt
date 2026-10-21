package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.viewmodel.DetalleUiState
import com.acuimuestra.app.viewmodel.DetalleViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.acuimuestra.app.viewmodel.FormularioCorreccion

@Composable
fun DetalleScreen(
    muestraId: Long,
    onVolver: () -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.factory(muestraId))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val muestra = estado.muestra

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp)) { Text("‹ Volver") }

        when {
            estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            muestra == null -> Text("No se encontró la muestra", style = MaterialTheme.typography.bodyLarge)
            else -> {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Muestra #${muestra.id}", style = MaterialTheme.typography.headlineSmall)
                    EtiquetaEstado(muestra.estado)
                }

                DatosMuestra(muestra)

                muestra.comentarioSupervisor?.let { comentario ->
                    Aviso(
                        titulo = "Comentario del supervisor",
                        texto = comentario,
                        esAlerta = muestra.estado == EstadoRevision.OBSERVADO
                    )
                }

                estado.mensaje?.let { Aviso(titulo = "Listo", texto = it, esAlerta = false) }

                Acciones(estado, viewModel)
            }
        }
    }
}

@Composable
private fun DatosMuestra(muestra: MuestraDetalle) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Dato("Centro", muestra.centroNombre)
            Dato("Tren · Línea", "${muestra.trenNombre} · ${muestra.lineaNombre}")
            Dato("Fecha y hora", muestra.fechaHora.comoFechaHora())
            Dato("Operador", muestra.operadorNombre)
            Dato("Tramo muestreado", "${muestra.tramoCm} cm")
            Dato("Conteo", "${muestra.conteo} individuos")
            muestra.calibrePromedioMm?.let { Dato("Calibre promedio", "$it mm") }
            muestra.pesoGramos?.let { Dato("Peso", "$it g") }
            Dato("Fotografía", if (muestra.fotoUri != null) "Adjunta" else "Sin fotografía")
            Dato("Observaciones", muestra.observaciones.ifBlank { "Sin observaciones" })
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Aviso(titulo: String, texto: String, esAlerta: Boolean) {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = if (esAlerta) colores.errorContainer else colores.primaryContainer,
        contentColor = if (esAlerta) colores.onErrorContainer else colores.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private enum class AccionConfirmar { VALIDAR, DESCARTAR }

@Composable
private fun Acciones(estado: DetalleUiState, viewModel: DetalleViewModel) {
    var accionPendiente by rememberSaveable { mutableStateOf<AccionConfirmar?>(null) }

    when {
        estado.puedeRevisar -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Revisión", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = estado.comentario,
                onValueChange = viewModel::onComentarioChange,
                label = { Text("Comentario (obligatorio para observar o descartar)") },
                minLines = 3,
                isError = estado.errorComentario != null,
                supportingText = estado.errorComentario?.let { mensaje -> { Text(mensaje) } },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = viewModel::observar, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Observar")
                }
                Button(
                    onClick = { accionPendiente = AccionConfirmar.VALIDAR },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Validar")
                }
            }
            OutlinedButton(
                onClick = { if (viewModel.comentarioEsValido()) accionPendiente = AccionConfirmar.DESCARTAR },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Descartar muestra")
            }
        }
        estado.puedeCorregir -> {
            val correccion = estado.correccion
            if (correccion == null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "El supervisor pidió correcciones. Revisa su comentario y corrige los datos.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = viewModel::iniciarCorreccion, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("Corregir datos")
                    }
                }
            } else {
                FormularioCorreccionUi(correccion, viewModel)
            }
        }
        estado.muestra?.estado == EstadoRevision.VALIDADO -> TextoCierre("Muestra validada: el registro está cerrado.")
        estado.muestra?.estado == EstadoRevision.DESCARTADO -> TextoCierre("Muestra descartada: el registro está cerrado.")
        estado.muestra?.estado == EstadoRevision.OBSERVADO -> TextoCierre("Esperando que el operador corrija la muestra.")
        else -> TextoCierre("Pendiente de revisión por el supervisor.")
    }

    accionPendiente?.let { accion ->
        val esValidar = accion == AccionConfirmar.VALIDAR
        AlertDialog(
            onDismissRequest = { accionPendiente = null },
            title = { Text(if (esValidar) "¿Validar muestra?" else "¿Descartar muestra?") },
            text = { Text("Esta acción cierra el registro y no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    if (esValidar) viewModel.validar() else viewModel.descartar()
                    accionPendiente = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { accionPendiente = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TextoCierre(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun FormularioCorreccionUi(correccion: FormularioCorreccion, viewModel: DetalleViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Corregir datos", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = correccion.conteo,
            onValueChange = viewModel::onConteoChange,
            label = { Text("Conteo de individuos *") },
            singleLine = true,
            isError = correccion.errorConteo != null,
            supportingText = correccion.errorConteo?.let { mensaje -> { Text(mensaje) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correccion.tramo,
            onValueChange = viewModel::onTramoChange,
            label = { Text("Tramo muestreado (cm) *") },
            singleLine = true,
            isError = correccion.errorTramo != null,
            supportingText = correccion.errorTramo?.let { mensaje -> { Text(mensaje) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correccion.observaciones,
            onValueChange = viewModel::onObservacionesChange,
            label = { Text("Observaciones") },
            minLines = 3,
            isError = correccion.errorObservaciones != null,
            supportingText = {
                Text(correccion.errorObservaciones ?: "${correccion.observaciones.length}/${DetalleViewModel.MAX_OBSERVACIONES}")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = viewModel::cancelarCorreccion, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("Cancelar")
            }
            Button(onClick = viewModel::guardarCorreccion, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("Guardar corrección")
            }
        }
    }
}