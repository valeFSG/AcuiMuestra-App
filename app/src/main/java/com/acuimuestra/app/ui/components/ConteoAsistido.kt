package com.acuimuestra.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.acuimuestra.app.viewmodel.Marca
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

private val ColorMarca = Color(0xFF2EE6C5)

@Composable
fun ConteoAsistido(
    fotoUri: String,
    marcas: List<Marca>,
    onAgregar: (Float, Float) -> Unit,
    onDeshacer: () -> Unit,
    onLimpiar: () -> Unit,
    onUsar: () -> Unit,
    onCerrar: () -> Unit
) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF05131F), contentColor = Color.White) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCerrar) { EtiquetaVolver(color = Color.White) }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        AnimatedContent(
                            targetState = marcas.size,
                            transitionSpec = {
                                (slideInVertically(tween(180)) { -it / 2 } + fadeIn(tween(180))) togetherWith
                                        (slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)))
                            },
                            label = "total"
                        ) { total ->
                            Text("$total", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = ColorMarca)
                        }
                        Text("individuos marcados", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Text(
                    "Toca cada chorito para marcarlo. Pellizca para ampliar la foto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    val zoom = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 8f))
                    ZoomableAsyncImage(
                        model = fotoUri,
                        contentDescription = "Fotografía de la muestra para conteo asistido",
                        state = rememberZoomableImageState(zoom),
                        modifier = Modifier.fillMaxSize(),
                        onClick = { toque ->
                            val limites = zoom.transformedContentBounds
                            if (limites.width > 0f && limites.contains(toque)) {
                                onAgregar(
                                    (toque.x - limites.left) / limites.width,
                                    (toque.y - limites.top) / limites.height
                                )
                            }
                        }
                    )
                    Canvas(Modifier.fillMaxSize()) {
                        val limites = zoom.transformedContentBounds
                        marcas.forEach { marca ->
                            val centro = Offset(
                                limites.left + marca.x * limites.width,
                                limites.top + marca.y * limites.height
                            )
                            drawCircle(ColorMarca.copy(alpha = 0.25f), radius = 14.dp.toPx(), center = centro)
                            drawCircle(ColorMarca, radius = 14.dp.toPx(), center = centro, style = Stroke(2.5.dp.toPx()))
                            drawCircle(ColorMarca, radius = 3.dp.toPx(), center = centro)
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeshacer,
                        enabled = marcas.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { TextoConIcono(Icons.AutoMirrored.Outlined.Undo, "Deshacer") }
                    OutlinedButton(
                        onClick = onLimpiar,
                        enabled = marcas.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Limpiar") }
                    Button(
                        onClick = onUsar,
                        enabled = marcas.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorMarca, contentColor = Color(0xFF05131F)),
                        modifier = Modifier.weight(1.4f).height(52.dp)
                    ) { Text("Usar ${marcas.size}", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}