package com.acuimuestra.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val pendiente = uriPendiente
        if (exito && pendiente != null) onFotoChange(pendiente)
        else pendiente?.let { borrarFoto(context, it) }
        uriPendiente = null
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val copia = withContext(Dispatchers.IO) { copiarAFotos(context, uri) }
            if (copia != null) onFotoChange(copia.toString())
            else mensaje = "No se pudo cargar la imagen"
        }
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
                    val uri = nuevaUriFoto(context)
                    uriPendiente = uri.toString()
                    runCatching { camara.launch(uri) }.onFailure {
                        borrarFoto(context, uri.toString())
                        uriPendiente = null
                        mensaje = "No hay una app de cámara disponible"
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("📷 Tomar foto") }

            OutlinedButton(
                onClick = {
                    mensaje = null
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f)
            ) { Text("🖼 Galería") }
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
    val context = LocalContext.current
    val imagen by produceState<ImageBitmap?>(null, fotoUri) {
        value = fotoUri?.let { withContext(Dispatchers.IO) { cargarImagen(context, Uri.parse(it)) } }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val actual = imagen
        if (actual != null) {
            Image(
                bitmap = actual,
                contentDescription = "Fotografía de la muestra",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Text(
                if (fotoUri == null) "Sin fotografía" else "Cargando…",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun carpetaFotos(context: Context) = File(context.filesDir, "fotos").apply { mkdirs() }

private fun nuevoArchivo(context: Context) =
    File(carpetaFotos(context), "muestra_${System.currentTimeMillis()}.jpg")

private fun uriDe(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

private fun nuevaUriFoto(context: Context): Uri = uriDe(context, nuevoArchivo(context))

private fun copiarAFotos(context: Context, origen: Uri): Uri? = runCatching {
    val destino = nuevoArchivo(context)
    context.contentResolver.openInputStream(origen)?.use { entrada ->
        destino.outputStream().use { salida -> entrada.copyTo(salida) }
    } ?: error("No se pudo abrir la imagen")
    uriDe(context, destino)
}.getOrNull()

private fun borrarFoto(context: Context, uri: String) {
    runCatching { context.contentResolver.delete(Uri.parse(uri), null, null) }
}

private fun cargarImagen(context: Context, uri: Uri, ladoMaximo: Int = 1024): ImageBitmap? = runCatching {
    val resolver = context.contentResolver
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }

    var reduccion = 1
    while (medidas.outWidth / (reduccion * 2) >= ladoMaximo || medidas.outHeight / (reduccion * 2) >= ladoMaximo) {
        reduccion *= 2
    }

    val bitmap = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = reduccion })
    } ?: error("No se pudo leer la imagen")

    val grados = resolver.openInputStream(uri)?.use {
        when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f

    val final = if (grados == 0f) bitmap
    else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
    final.asImageBitmap()
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