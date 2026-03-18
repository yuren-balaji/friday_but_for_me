package com.example.first_app_0_0_1.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.first_app_0_0_1.data.Note
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GraphScreen(notes: List<Note>, modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    val textMeasurer = rememberTextMeasurer()
    
    // Capture values from MaterialTheme here, in the Composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceColor)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    offset += pan
                    scale *= zoom
                }
            }
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
        ) {
            if (notes.isEmpty()) return@Canvas

            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = 300f

            val positions = notes.mapIndexed { index, note ->
                val angle = 2 * Math.PI * index / notes.size
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                note.id to Offset(x, y)
            }.toMap()

            // Draw connections (placeholder for wiki-links)
            positions.values.forEach { start ->
                positions.values.forEach { end ->
                    if (start != end) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f),
                            start = start,
                            end = end,
                            strokeWidth = 1f
                        )
                    }
                }
            }

            // Draw nodes
            notes.forEach { note ->
                val pos = positions[note.id] ?: Offset.Zero
                drawCircle(
                    color = primaryColor,
                    radius = 20f,
                    center = pos
                )
                // drawText is NOT a Composable, but its 'style' parameter was previously using MaterialTheme (a Composable)
                // Now it uses labelStyle, which was captured outside this block.
                drawText(
                    textMeasurer = textMeasurer,
                    text = note.title,
                    topLeft = pos.copy(y = pos.y + 25f),
                    style = labelStyle
                )
            }
        }
    }
}
