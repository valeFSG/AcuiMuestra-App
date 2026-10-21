package com.acuimuestra.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.data.model.Rol
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.ui.theme.VerdeOscuro
import com.acuimuestra.app.ui.theme.VerdeSecundario
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
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "AcuiMuestra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::cerrarSesion) {
                        Text("Cerrar sesión", color = Color.White)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Hola,", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                Text(
                    actual.nombreVisible,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Surface(color = Color.White.copy(alpha = 0.18f), shape = CircleShape) {
                    Text(
                        if (esSupervisor) "Supervisor técnico" else "Operador de muestreo",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Column(
            Modifier.padding(24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Qué quieres hacer hoy?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            if (esSupervisor) {
                CardAccion(
                    titulo = "Revisar muestras",
                    descripcion = "Valida, observa o descarta los registros",
                    simbolo = "✓",
                    colores = listOf(VerdeOscuro, VerdeSecundario),
                    onClick = onHistorial
                )
            } else {
                CardAccion(
                    titulo = "Nueva muestra",
                    descripcion = "Registra una muestra de chorito en terreno",
                    simbolo = "+",
                    colores = listOf(AzulProfundo, AzulPrincipal),
                    onClick = onNuevaMuestra
                )
                CardAccion(
                    titulo = "Historial",
                    descripcion = "Consulta y filtra las muestras registradas",
                    simbolo = "≡",
                    colores = listOf(VerdeOscuro, VerdeSecundario),
                    onClick = onHistorial
                )
            }
        }
    }
}

@Composable
private fun CardAccion(
    titulo: String,
    descripcion: String,
    simbolo: String,
    colores: List<Color>,
    onClick: () -> Unit,
    @DrawableRes imagen: Int? = null
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colores))
            .clickable(onClick = onClick)
    ) {
        if (imagen != null) {
            Image(
                painter = painterResource(imagen),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))))
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(simbolo, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
        }
    }
}