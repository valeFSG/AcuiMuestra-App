package com.acuimuestra.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.acuimuestra.app.ui.theme.AcuiMuestraTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFechaHora(
    fechaHora: Long,
    onFechaHoraChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    var mostrarHora by rememberSaveable { mutableStateOf(false) }
    var fechaElegidaUtc by rememberSaveable { mutableStateOf<Long?>(null) }

    val interaccion = remember { MutableInteractionSource() }
    LaunchedEffect(interaccion) {
        interaccion.interactions.collect {
            if (it is PressInteraction.Release) mostrarFecha = true
        }
    }

    OutlinedTextField(
        value = formatear(fechaHora),
        onValueChange = {},
        readOnly = true,
        label = { Text("Fecha y hora del muestreo") },
        trailingIcon = { Text("📅") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        interactionSource = interaccion,
        modifier = modifier.fillMaxWidth()
    )

    if (mostrarFecha) {
        val hoyUtc = aUtcMedianoche(System.currentTimeMillis())
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = aUtcMedianoche(fechaHora),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= hoyUtc
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        fechaElegidaUtc = estadoFecha.selectedDateMillis
                        mostrarFecha = false
                        mostrarHora = true
                    },
                    enabled = estadoFecha.selectedDateMillis != null
                ) { Text("Siguiente") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (mostrarHora) {
        val actual = Calendar.getInstance().apply { timeInMillis = fechaHora }
        val estadoHora = rememberTimePickerState(
            initialHour = actual.get(Calendar.HOUR_OF_DAY),
            initialMinute = actual.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { mostrarHora = false },
            title = { Text("Hora del muestreo") },
            text = { TimePicker(state = estadoHora) },
            confirmButton = {
                TextButton(onClick = {
                    fechaElegidaUtc?.let {
                        onFechaHoraChange(combinar(it, estadoHora.hour, estadoHora.minute))
                    }
                    mostrarHora = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarHora = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun formatear(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(millis))

private fun aUtcMedianoche(millisLocal: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = millisLocal }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun combinar(fechaUtc: Long, hora: Int, minuto: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = fechaUtc }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hora, minuto)
    }.timeInMillis
}
@Preview(showBackground = true)
@Composable
private fun SelectorFechaHoraPreview() {
    var fecha by remember { mutableLongStateOf(System.currentTimeMillis()) }
    AcuiMuestraTheme {
        SelectorFechaHora(
            fechaHora = fecha,
            onFechaHoraChange = { fecha = it },
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
        )
    }
}

