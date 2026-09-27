package com.example.pathbuilder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/** Hand-drawn line icons, so the app needs no icon library or image assets. */
enum class Glyph { BACK, ROTATE, UNDO, RESTART }

@Composable
fun GlyphIcon(glyph: Glyph, tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier.size(size)) {
        val w = this.size.minDimension
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (glyph) {
            Glyph.BACK -> drawPath(
                Path().apply {
                    moveTo(w * 0.62f, w * 0.2f)
                    lineTo(w * 0.32f, w * 0.5f)
                    lineTo(w * 0.62f, w * 0.8f)
                },
                tint, style = stroke,
            )
            Glyph.ROTATE -> circularArrow(tint, stroke, w, startDeg = -60f, sweepDeg = 290f, clockwise = true)
            Glyph.RESTART -> circularArrow(tint, stroke, w, startDeg = 240f, sweepDeg = -290f, clockwise = false)
            Glyph.UNDO -> {
                drawPath(
                    Path().apply {
                        moveTo(w * 0.22f, w * 0.42f)
                        lineTo(w * 0.62f, w * 0.42f)
                        cubicTo(w * 0.9f, w * 0.42f, w * 0.9f, w * 0.8f, w * 0.62f, w * 0.8f)
                        lineTo(w * 0.42f, w * 0.8f)
                    },
                    tint, style = stroke,
                )
                drawPath(
                    Path().apply {
                        moveTo(w * 0.4f, w * 0.24f)
                        lineTo(w * 0.22f, w * 0.42f)
                        lineTo(w * 0.4f, w * 0.6f)
                    },
                    tint, style = stroke,
                )
            }
        }
    }
}

private fun DrawScope.circularArrow(tint: Color, stroke: Stroke, w: Float, startDeg: Float, sweepDeg: Float, clockwise: Boolean) {
    val r = w * 0.3f
    val c = Offset(w / 2, w / 2)
    drawArc(tint, startDeg, sweepDeg, false, topLeft = c - Offset(r, r), size = Size(r * 2, r * 2), style = stroke)
    val endRad = Math.toRadians((startDeg + sweepDeg).toDouble())
    val tip = Offset(c.x + r * cos(endRad).toFloat(), c.y + r * sin(endRad).toFloat())
    // tangent direction of travel at the arc end
    val sign = if (clockwise) 1f else -1f
    val tangent = Offset(-sin(endRad).toFloat() * sign, cos(endRad).toFloat() * sign)
    val normal = Offset(cos(endRad).toFloat(), sin(endRad).toFloat())
    val head = w * 0.16f
    drawPath(
        Path().apply {
            moveTo(tip.x - tangent.x * head + normal.x * head, tip.y - tangent.y * head + normal.y * head)
            lineTo(tip.x, tip.y)
            lineTo(tip.x - tangent.x * head - normal.x * head, tip.y - tangent.y * head - normal.y * head)
        },
        tint, style = stroke,
    )
}
