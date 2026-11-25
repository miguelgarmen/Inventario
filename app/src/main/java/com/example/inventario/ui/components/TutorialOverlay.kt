package com.example.inventario.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TutorialStep(
    val titulo: String,
    val descripcion: String,
    val targetRect: Rect? = null,
    val alignBottom: Boolean = false,
    val actionRoute: String? = null // <-- NUEVO: Para navegar automáticamente
)

@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    currentStep: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (currentStep !in steps.indices) return
    val step = steps[currentStep]

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))

                if (step.targetRect != null) {
                    val padding = 8.dp.toPx()
                    val rectConPadding = Rect(
                        step.targetRect.left - padding,
                        step.targetRect.top - padding,
                        step.targetRect.right + padding,
                        step.targetRect.bottom + padding
                    )
                    addRoundRect(RoundRect(rectConPadding, CornerRadius(16.dp.toPx())))
                }
            }
            drawPath(path, Color.Black.copy(alpha = 0.75f))

            if (step.targetRect != null) {
                val padding = 8.dp.toPx()
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(step.targetRect.left - padding, step.targetRect.top - padding),
                    size = Size(step.targetRect.width + padding * 2, step.targetRect.height + padding * 2),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        Card(
            modifier = Modifier
                .align(
                    if (step.targetRect == null) Alignment.Center
                    else if (step.alignBottom) Alignment.BottomCenter
                    else Alignment.TopCenter
                )
                .padding(24.dp)
                .fillMaxWidth()
                .padding(top = if (step.alignBottom) 0.dp else 120.dp, bottom = if (step.alignBottom) 120.dp else 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = step.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onSkip) {
                        Text("Saltar tutorial", color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = onNext) {
                        Text(if (currentStep == steps.size - 1) "¡Empezar a usar la app!" else "Siguiente")
                    }
                }
            }
        }
    }
}