package com.acuimuestra.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

interface AlmacenFotos {
    suspend fun leerParaEnviar(uri: String): ByteArray?
    suspend fun guardarRecibida(uuid: String, datos: ByteArray): String?
}

class AlmacenFotosAndroid(private val context: Context) : AlmacenFotos {

    override suspend fun leerParaEnviar(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { comprimir(Uri.parse(uri)) }.getOrNull()
    }

    override suspend fun guardarRecibida(uuid: String, datos: ByteArray): String? = withContext(Dispatchers.IO) {
        runCatching {
            val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
            val archivo = File(carpeta, "remota_$uuid.jpg").apply { writeBytes(datos) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo).toString()
        }.getOrNull()
    }

    private fun comprimir(uri: Uri): ByteArray? {
        val resolver = context.contentResolver
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
        var muestreo = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
        val original = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestreo })
        } ?: return null
        val grados = resolver.openInputStream(uri)?.use { rotacion(ExifInterface(it)) } ?: 0f
        val escala = minOf(1f, LADO_MAXIMO.toFloat() / maxOf(original.width, original.height))
        val matriz = Matrix().apply {
            postScale(escala, escala)
            postRotate(grados)
        }
        val ajustada = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        return ByteArrayOutputStream().use { salida ->
            ajustada.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)
            salida.toByteArray()
        }
    }

    private fun rotacion(exif: ExifInterface): Float =
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

    private companion object {
        const val LADO_MAXIMO = 1280
        const val CALIDAD = 80
    }
}
