package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.EstadoRevision
import com.acuimuestra.app.data.model.MuestraDetalle
import com.acuimuestra.app.ui.comoFechaHora
import com.acuimuestra.app.ui.components.FotoMuestra
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.viewmodel.DetalleUiState
import com.acuimuestra.app.viewmodel.DetalleViewModel
import com.acuimuestra.app.viewmodel.FormularioCorreccion
import androidx.compose.ui.platform.LocalContext
import com.acuimuestra.app.ui.compartirTexto
import com.acuimuestra.app.ui.resumenTexto
import com.acuimuestra.app.ui.components.SelectorFoto

@Composable
fun DetalleScreen(
    muestraId: Long,
    onVolver: () -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.factory(muestraId))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val muestra = estado.muestra

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        when {
            estado.cargando -> Box(
                Modifier.fillMaxWidth().statusBarsPadding().padding(48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            muestra == null -> Column(Modifier.statusBarsPadding().padding(24.dp)) {
                TextButton(onClick = onVolver, contentPadding = PaddingValues(0.dp)) { Text("‹ Volver") }
                Text("No se encontró la muestra", style = MaterialTheme.typography.bodyLarge)
            }

            else -> {
                Encabezado(muestra, onVolver)
                Column(
                    Modifier.padding(20.dp).navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Metricas(muestra)

                    Seccion("Ubicación") {
                        Dato("Centro de cultivo", muestra.centroNombre)
                        Dato("Tren · Línea", "${muestra.trenNombre} · ${muestra.lineaNombre}")
                    }
                    Seccion("Registro") {
                        Dato("Fecha y hora", muestra.fechaHora.comoFechaHora())
                        Dato("Operador", muestra.operadorNombre)
                        Dato("Fotografía", if (muestra.fotoUri != null) "Adjunta" else "Sin fotografía")
                    }
                    Seccion("Observaciones") {
                        Text(
                            muestra.observaciones.ifBlank { "Sin observaciones" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (muestra.observaciones.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = { compartirTexto(context, muestra.resumenTexto()) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Compartir resumen")
                    }

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
}

@Composable
private fun Encabezado(muestra: MuestraDetalle, onVolver: () -> Unit) {
    val conFoto = muestra.fotoUri != null
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (conFoto) 300.dp else 200.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
    ) {
        muestra.fotoUri?.let { uri ->
            FotoMuestra(fotoUri = uri, modifier = Modifier.matchParentSize())
        }
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        TextButton(
            onClick = onVolver,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp)
        ) {
            Text("‹ Volver", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            EtiquetaEstado(muestra.estado)
            Spacer(Modifier.height(8.dp))
            Text(
                "Muestra #${muestra.id}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "${muestra.centroNombre} · ${muestra.lineaNombre}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun Metricas(muestra: MuestraDetalle) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Conteo", "${muestra.conteo}", "individuos", destacada = true, modifier = Modifier.weight(1f))
            Metrica("Tramo", "${muestra.tramoCm}", "cm", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Calibre", muestra.calibrePromedioMm?.toString() ?: "—", "mm", modifier = Modifier.weight(1f))
            Metrica("Peso", muestra.pesoGramos?.toString() ?: "—", "g", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun Metrica(
    etiqueta: String,
    valor: String,
    unidad: String,
    modifier: Modifier = Modifier,
    destacada: Boolean = false
) {
    val colores = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (destacada) colores.primary else colores.surfaceContainerLowest,
            contentColor = if (destacada) colores.onPrimary else colores.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = LocalContentColor.current.copy(alpha = 0.75f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (valor != "—") {
                    Text(
                        " $unidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                titulo.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
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
        Text("Corregir datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoCorreccion(correccion.conteo, viewModel::onConteoChange, "Conteo *", correccion.errorConteo, false, Modifier.weight(1f))
            CampoCorreccion(correccion.tramo, viewModel::onTramoChange, "Tramo (cm) *", correccion.errorTramo, false, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoCorreccion(correccion.calibre, viewModel::onCalibreChange, "Calibre (mm)", correccion.errorCalibre, true, Modifier.weight(1f))
            CampoCorreccion(correccion.peso, viewModel::onPesoChange, "Peso (g)", correccion.errorPeso, true, Modifier.weight(1f))
        }

        OutlinedTextField(
            value = correccion.observaciones,
            onValueChange = viewModel::onObservacionesChange,
            label = { Text("Observaciones") },
            minLines = 3,
            isError = correccion.errorObservaciones != null,
            supportingText = {
                Text(correccion.errorObservaciones ?: "${correccion.observaciones.length}/${DetalleViewModel.MAX_OBSERVACIONES}")
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        SelectorFoto(fotoUri = correccion.fotoUri, onFotoChange = viewModel::onFotoChange)

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

@Composable
private fun CampoCorreccion(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    decimal: Boolean,
    modifier: Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { mensaje -> { Text(mensaje) } },
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}