package com.acuimuestra.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acuimuestra.app.R
import com.acuimuestra.app.ui.components.EtiquetaVolver
import com.acuimuestra.app.ui.theme.AmbarContenedor
import com.acuimuestra.app.ui.theme.AmbarOscuro
import com.acuimuestra.app.ui.theme.AzulContenedor
import com.acuimuestra.app.ui.theme.AzulPrincipal
import com.acuimuestra.app.ui.theme.AzulProfundo
import com.acuimuestra.app.ui.theme.VerdeContenedor
import com.acuimuestra.app.ui.theme.VerdeOscuro
import com.acuimuestra.app.ui.theme.VerdeSecundario
import com.acuimuestra.app.viewmodel.LoginViewModel

private data class PerfilLogin(
    val nombre: String,
    val rol: String,
    val usuario: String,
    val icono: ImageVector,
    val fondo: Color,
    val acento: Color,
    val textoFuerte: Color
)

private val perfiles = listOf(
    PerfilLogin("Camila Rojas", "Operadora de muestreo", "operador", Icons.Outlined.Engineering, AzulContenedor, AzulPrincipal, AzulProfundo),
    PerfilLogin("Diego Muñoz", "Operador de muestreo", "operador2", Icons.Outlined.Engineering, AzulContenedor, AzulPrincipal, AzulProfundo),
    PerfilLogin("Andrea Soto", "Supervisora técnica", "supervisor", Icons.AutoMirrored.Outlined.FactCheck, VerdeContenedor, VerdeSecundario, VerdeOscuro),
    PerfilLogin("Felipe Vargas", "Analista de datos", "analista", Icons.Outlined.BarChart, AmbarContenedor, AmbarOscuro, AmbarOscuro)
)

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var claveVisible by rememberSaveable { mutableStateOf(false) }
    val perfil = perfiles.find { it.usuario == estado.usuario }

    LaunchedEffect(estado.ingresoExitoso) {
        if (estado.ingresoExitoso) {
            viewModel.navegacionRealizada()
            onLoginExitoso()
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(AzulProfundo, AzulPrincipal)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .size(96.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_acuimuestra),
                    contentDescription = "Logo de AcuiMuestra",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(74.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "AcuiMuestra",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Registro y trazabilidad de muestras",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (perfil == null) {
                        Text("¿Quién eres?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Elige tu perfil para continuar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        perfiles.forEach { p ->
                            TarjetaPerfil(p, seleccionada = false) {
                                claveVisible = false
                                viewModel.onClaveChange("")
                                viewModel.onUsuarioChange(p.usuario)
                            }
                        }
                    } else {
                        TextButton(
                            onClick = {
                                viewModel.onClaveChange("")
                                viewModel.onUsuarioChange("")
                            },
                            enabled = !estado.cargando,
                            contentPadding = PaddingValues(0.dp)
                        ) { EtiquetaVolver("Cambiar perfil") }

                        TarjetaPerfil(perfil, seleccionada = true, onClick = {})

                        OutlinedTextField(
                            value = estado.clave,
                            onValueChange = viewModel::onClaveChange,
                            label = { Text("Contraseña") },
                            singleLine = true,
                            enabled = !estado.cargando,
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { claveVisible = !claveVisible }) {
                                    Icon(
                                        if (claveVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.ingresar() }),
                            isError = estado.error != null,
                            supportingText = estado.error?.let { mensaje -> { Text(mensaje) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = viewModel::ingresar,
                            enabled = !estado.cargando,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (estado.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Ingresar", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPerfil(perfil: PerfilLogin, seleccionada: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = !seleccionada,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) perfil.fondo else MaterialTheme.colorScheme.surfaceContainerLowest,
            disabledContainerColor = perfil.fondo
        ),
        border = BorderStroke(
            if (seleccionada) 2.dp else 1.dp,
            if (seleccionada) perfil.acento else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (seleccionada) Color.White else perfil.fondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(perfil.icono, contentDescription = null, tint = perfil.acento)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    perfil.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (seleccionada) perfil.textoFuerte else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    perfil.rol,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (seleccionada) perfil.textoFuerte else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                if (seleccionada) Icons.Outlined.Check else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = if (seleccionada) perfil.acento else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}