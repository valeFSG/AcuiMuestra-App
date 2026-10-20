package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.Rol
import com.acuimuestra.app.viewmodel.InicioViewModel

@Composable
fun InicioScreen(
    onNuevaMuestra: () -> Unit,
    onHistorial: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: InicioViewModel = viewModel(factory = InicioViewModel.Factory)
) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()

    LaunchedEffect(usuario) {
        if (usuario == null) onCerrarSesion()
    }

    val actual = usuario ?: return
    val esSupervisor = actual.rol == Rol.SUPERVISOR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Hola, ${actual.nombreVisible}", style = MaterialTheme.typography.headlineSmall)
        Text(
            if (esSupervisor) "Supervisor técnico" else "Operador de muestreo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        if (esSupervisor) {
            OpcionInicio("Revisar muestras", "Valida, observa o corrige los registros", onHistorial)
        } else {
            OpcionInicio("Nueva muestra", "Registra una muestra en terreno", onNuevaMuestra)
            OpcionInicio("Historial", "Consulta las muestras registradas", onHistorial)
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = viewModel::cerrarSesion,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun OpcionInicio(titulo: String, descripcion: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}