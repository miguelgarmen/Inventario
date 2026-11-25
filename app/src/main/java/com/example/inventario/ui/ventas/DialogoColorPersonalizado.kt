package com.example.inventario.ui.ventas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Diálogo para elegir un color personalizado con una rueda de colores clásica.
 *  - Ángulo de la rueda = Matiz (hue)
 *  - Distancia al centro = Saturación
 *  - Slider aparte = Luminosidad
 * Devuelve el color elegido como Long en formato ARGB (0xAARRGGBB).
 */
@Composable
fun DialogoColorPersonalizado(
    colorInicial: Long? = null,
    onDismiss: () -> Unit,
    onColorElegido: (Long) -> Unit
) {
    // Convertimos el color inicial a HSL para posicionar el indicador
    val (hInit, sInit, lInit) = remember {
        if (colorInicial != null) {
            val argb = colorInicial.toInt()
            val r = android.graphics.Color.red(argb) / 255f
            val g = android.graphics.Color.green(argb) / 255f
            val b = android.graphics.Color.blue(argb) / 255f
            rgbToHsl(r, g, b)
        } else {
            Triple(0f, 1f, 0.5f) // Rojo vivo por defecto
        }
    }

    var hue by remember { mutableFloatStateOf(hInit) }        // 0 - 360
    var saturation by remember { mutableFloatStateOf(sInit) } // 0 - 1
    var lightness by remember { mutableFloatStateOf(lInit) }  // 0 - 1

    val colorActual = Color.hsl(hue, saturation, lightness)
    val colorSaturadoMax = Color.hsl(hue, saturation, 0.5f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige tu color") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rueda de colores
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RuedaDeColor(
                        hue = hue,
                        saturacion = saturation,
                        lightness = lightness,
                        onCambio = { h, s ->
                            hue = h
                            saturation = s
                        }
                    )
                }

                // Vista previa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Color:", fontWeight = FontWeight.SemiBold)
                    Box(
                        Modifier
                            .size(48.dp)
                            .background(colorActual, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }

                // Slider de luminosidad
                Column(Modifier.fillMaxWidth()) {
                    Text("Luminosidad", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = lightness,
                        onValueChange = { lightness = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = colorSaturadoMax,
                            activeTrackColor = colorActual
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val argb = colorActual.toArgb().toLong() and 0xFFFFFFFFL
                    onColorElegido(argb)
                }
            ) { Text("Usar color") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun RuedaDeColor(
    hue: Float,
    saturacion: Float,
    lightness: Float,
    onCambio: (Float, Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    val (h, s) = posicionAColor(pos, size)
                    onCambio(h, s)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val (h, s) = posicionAColor(change.position, size)
                    onCambio(h, s)
                }
            }
    ) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        val radio = size.width / 2f

        // 1) Rueda de colores (matiz en ángulo)
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red,     // 0°
                    Color.Yellow,  // 60°
                    Color.Green,   // 120°
                    Color.Cyan,    // 180°
                    Color.Blue,    // 240°
                    Color.Magenta, // 300°
                    Color.Red      // 360°
                ),
                center = centro
            ),
            radius = radio
        )

        // 2) Degradado radial blanco para simular saturación
        //    (centro blanco = sin saturación, borde = máxima saturación)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = centro,
                radius = radio
            ),
            radius = radio
        )

        // 3) Indicador de posición actual
        val anguloRad = Math.toRadians(hue.toDouble())
        val x = centro.x + cos(anguloRad).toFloat() * saturacion * radio
        val y = centro.y + sin(anguloRad).toFloat() * saturacion * radio

        // Círculo negro por fuera
        drawCircle(
            color = Color.Black,
            radius = 11.dp.toPx(),
            center = Offset(x, y),
            style = Stroke(width = 4.dp.toPx())
        )
        // Círculo blanco por dentro
        drawCircle(
            color = Color.White,
            radius = 11.dp.toPx(),
            center = Offset(x, y),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/** Traduce una posición dentro del canvas a (hue, saturación). */
private fun posicionAColor(pos: Offset, size: IntSize): Pair<Float, Float> {
    val centro = Offset(size.width / 2f, size.height / 2f)
    val dx = pos.x - centro.x
    val dy = pos.y - centro.y
    val distancia = hypot(dx, dy)
    val radio = size.width / 2f
    val sat = (distancia / radio).coerceIn(0f, 1f)
    var angulo = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angulo < 0) angulo += 360f
    return angulo to sat
}

/** Convierte RGB (0-1) a HSL. Devuelve Triple(hue 0-360, sat 0-1, light 0-1). */
private fun rgbToHsl(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    when {
        delta == 0f -> h = 0f
        max == r -> h = 60f * (((g - b) / delta) % 6f)
        max == g -> h = 60f * (((b - r) / delta) + 2f)
        max == b -> h = 60f * (((r - g) / delta) + 4f)
    }
    if (h < 0) h += 360f

    val l = (max + min) / 2f
    val s = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * l - 1f))

    return Triple(h, s, l)
}