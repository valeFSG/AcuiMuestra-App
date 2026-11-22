package com.acuimuestra.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CamaraCaptura(onFotoTomada: (String) -> Unit, onCerrar: () -> Unit) {
    val context = LocalContext.current
    var permiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permiso = it }
    LaunchedEffect(Unit) { if (!permiso) pedirPermiso.launch(Manifest.permission.CAMERA) }

    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = Color.Black, contentColor = Color.White) {
            if (permiso) {
                VistaCamara(onFotoTomada, onCerrar)
            } else {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Para tomar la foto de la muestra, AcuiMuestra necesita permiso para usar la cámara.", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { pedirPermiso.launch(Manifest.permission.CAMERA) }) { Text("Dar permiso") }
                    TextButton(onClick = onCerrar) { Text("Cancelar", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun VistaCamara(onFotoTomada: (String) -> Unit, onCerrar: () -> Unit) {
    val context = LocalContext.current
    val ciclo = LocalLifecycleOwner.current
    val vistaPrevia = remember { PreviewView(context) }
    val captura = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    var tomando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(ciclo) {
        val futuro = ProcessCameraProvider.getInstance(context)
        futuro.addListener({
            val proveedor = futuro.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(vistaPrevia.surfaceProvider) }
            runCatching {
                proveedor.unbindAll()
                proveedor.bindToLifecycle(ciclo, CameraSelector.DEFAULT_BACK_CAMERA, preview, captura)
            }.onFailure { error = "No se pudo abrir la cámara" }
        }, ContextCompat.getMainExecutor(context))
        onDispose { runCatching { futuro.get().unbindAll() } }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { vistaPrevia }, modifier = Modifier.fillMaxSize())

        TextButton(
            onClick = onCerrar,
            modifier = Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(8.dp)
        ) { TextoConIcono(Icons.Outlined.Close, "Cerrar", Color.White) }

        error?.let {
            Text(
                it,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val interaccion = remember { MutableInteractionSource() }
        val presionado by interaccion.collectIsPressedAsState()
        val escala by animateFloatAsState(if (presionado || tomando) 0.88f else 1f, label = "obturador")

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 32.dp)
                .size(78.dp)
                .scale(escala)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(interactionSource = interaccion, indication = null, enabled = !tomando) {
                    tomando = true
                    error = null
                    val archivo = nuevoArchivoFoto(context)
                    captura.takePicture(
                        ImageCapture.OutputFileOptions.Builder(archivo).build(),
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                                onFotoTomada(uriDeFoto(context, archivo).toString())
                            }

                            override fun onError(excepcion: ImageCaptureException) {
                                archivo.delete()
                                tomando = false
                                error = "No se pudo tomar la foto"
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (tomando) CircularProgressIndicator(Modifier.size(28.dp), color = Color.Black, strokeWidth = 3.dp)
        }
    }
}