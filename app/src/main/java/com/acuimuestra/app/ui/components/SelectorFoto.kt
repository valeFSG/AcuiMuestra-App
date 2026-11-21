package com.acuimuestra.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.SubcomposeAsyncImage
import com.acuimuestra.app.ui.theme.AcuiMuestraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SelectorFoto(
    fotoUri: String?,
    onFotoChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mostrarCamara by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val copia = withContext(Dispatchers.IO) { copiarAFotos(context, uri) }
            if (copia != null) onFotoChange(copia.toString())
            else mensaje = "No se pudo cargar la imagen"
        }
    }

    if (mostrarCamara) {
        CamaraCaptura(
            onFotoTomada = {
                mostrarCamara = false
                onFotoChange(it)
            },
            onCerrar = { mostrarCamara = false }
        )
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Fotografía de la muestra", style = MaterialTheme.typography.titleMedium)

        FotoMuestra(
            fotoUri = fotoUri,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(
                    1.dp,
                    if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(16.dp)
                )
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    mensaje = null
                    mostrarCamara = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Tomar foto")
            }

            OutlinedButton(
                onClick = {
                    mensaje = null
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Galería")
            }
        }

        if (fotoUri != null) {
            TextButton(onClick = { onFotoChange(null) }) { Text("Quitar foto") }
        }

        (error ?: mensaje)?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun FotoMuestra(fotoUri: String?, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (fotoUri == null) {
            Text("Sin fotografía", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            SubcomposeAsyncImage(
                model = fotoUri,
                contentDescription = "Fotografía de la muestra",
                contentScale = ContentScale.Crop,
                loading = { Text("Cargando…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                error = { Text("No se pudo mostrar la foto", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

private fun carpetaFotos(context: Context) = File(context.filesDir, "fotos").apply { mkdirs() }

internal fun nuevoArchivoFoto(context: Context) =
    File(carpetaFotos(context), "muestra_${System.currentTimeMillis()}.jpg")

internal fun uriDeFoto(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

private fun copiarAFotos(context: Context, origen: Uri): Uri? = runCatching {
    val destino = nuevoArchivoFoto(context)
    context.contentResolver.openInputStream(origen)?.use { entrada ->
        destino.outputStream().use { salida -> entrada.copyTo(salida) }
    } ?: error("No se pudo abrir la imagen")
    uriDeFoto(context, destino)
}.getOrNull()

@Preview(showBackground = true)
@Composable
private fun SelectorFotoPreview() {
    var foto by remember { mutableStateOf<String?>(null) }
    AcuiMuestraTheme {
        SelectorFoto(
            fotoUri = foto,
            onFotoChange = { foto = it },
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
        )
    }
}
