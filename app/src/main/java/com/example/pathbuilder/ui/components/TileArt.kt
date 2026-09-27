package com.example.pathbuilder.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.pathbuilder.game.Direction
import com.example.pathbuilder.game.PathCell
import com.example.pathbuilder.ui.theme.BoardPalette

/*
 * All drawing works in the local square of one cell (size.minDimension), so the same
 * functions render the board, tray pieces, the splash animation and How-to-Play examples.
 */

private const val TILE_INSET = 0.06f
private const val TILE_RADIUS = 0.16f
private const val GROOVE_WIDTH = 0.22f
private const val LINE_WIDTH = 0.085f

private val DrawScope.side get() = size.minDimension

private fun DrawScope.tileRect(inset: Float = side * TILE_INSET) =
    Pair(Offset(inset, inset), Size(side - inset * 2, side - inset * 2))

fun DrawScope.drawWell(palette: BoardPalette, hint: Boolean = false) {
    val (topLeft, rect) = tileRect(side * 0.045f)
    val radius = CornerRadius(side * 0.14f)
    drawRoundRect(palette.wellEdge, topLeft, rect, radius)
    val lip = side * 0.025f
    drawRoundRect(palette.well, topLeft + Offset(0f, lip), Size(rect.width, rect.height - lip), radius)
    if (hint) drawCircle(palette.hint, radius = side * 0.05f, center = Offset(side / 2, side / 2))
}

fun DrawScope.drawBlocked(palette: BoardPalette) {
    val (topLeft, rect) = tileRect(side * 0.045f)
    val shape = RoundRect(topLeft.x, topLeft.y, topLeft.x + rect.width, topLeft.y + rect.height, CornerRadius(side * 0.14f))
    drawRoundRect(palette.blocked, topLeft, rect, CornerRadius(side * 0.14f))
    clipPath(Path().apply { addRoundRect(shape) }) {
        val step = side * 0.16f
        var x = -side
        while (x < side * 2) {
            drawLine(palette.blockedHatch, Offset(x, side), Offset(x + side, 0f), strokeWidth = side * 0.035f)
            x += step
        }
    }
}

/** A physical piece: drop shadow, bevelled rim lit from the top-left, flat face. */
fun DrawScope.drawTileBody(
    face: Color,
    light: Color,
    edge: Color,
    shadow: Color,
    elevation: Float = 1f,
) {
    val (topLeft, rect) = tileRect()
    val radius = CornerRadius(side * TILE_RADIUS)
    drawRoundRect(shadow, topLeft + Offset(0f, side * (0.02f + 0.03f * elevation)), rect, radius)
    drawRoundRect(
        brush = Brush.linearGradient(listOf(light, face, edge), start = topLeft, end = topLeft + Offset(rect.width, rect.height)),
        topLeft = topLeft,
        size = rect,
        cornerRadius = radius,
    )
    val bevel = side * 0.045f
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(face, lerpColor(face, edge, 0.25f)), startY = topLeft.y, endY = topLeft.y + rect.height),
        topLeft = topLeft + Offset(bevel, bevel),
        size = Size(rect.width - bevel * 2, rect.height - bevel * 2),
        cornerRadius = CornerRadius(side * TILE_RADIUS - bevel),
    )
}

fun DrawScope.drawTile(palette: BoardPalette, connections: Set<Direction>, routeColor: Color, elevation: Float = 1f, rotation: Float = 0f) {
    drawTileBody(palette.tileFace, palette.tileLight, palette.tileEdge, palette.tileShadow, elevation)
    drawRoute(connections, palette.groove, routeColor, rotation)
}

/** Groove channel + route line, both centred in the tile and meeting its edges at the midpoints. */
fun DrawScope.drawRoute(connections: Set<Direction>, groove: Color, line: Color, rotation: Float = 0f) {
    rotate(rotation, pivot = Offset(side / 2, side / 2)) {
        drawRouteStroke(connections, groove, side * GROOVE_WIDTH)
        drawRouteStroke(connections, line, side * LINE_WIDTH)
    }
}

private fun DrawScope.drawRouteStroke(connections: Set<Direction>, color: Color, width: Float) {
    val a = side * TILE_INSET
    val b = side - a
    val c = side / 2
    val half = (b - a) / 2
    val horizontal = setOf(Direction.LEFT, Direction.RIGHT)
    val vertical = setOf(Direction.UP, Direction.DOWN)
    when {
        connections == horizontal -> drawLine(color, Offset(a, c), Offset(b, c), width, StrokeCap.Butt)
        connections == vertical -> drawLine(color, Offset(c, a), Offset(c, b), width, StrokeCap.Butt)
        connections.size == 2 -> {
            val cx = if (Direction.RIGHT in connections) b else a
            val cy = if (Direction.DOWN in connections) b else a
            val startAngle = when {
                Direction.UP in connections && Direction.RIGHT in connections -> 90f
                Direction.RIGHT in connections && Direction.DOWN in connections -> 180f
                Direction.DOWN in connections && Direction.LEFT in connections -> 270f
                else -> 0f
            }
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx - half, cy - half),
                size = Size(half * 2, half * 2),
                style = Stroke(width, cap = StrokeCap.Butt),
            )
        }
        else -> connections.forEach { d -> drawLine(color, Offset(c, c), edgeMid(d, a, b, c), width, StrokeCap.Butt) }
    }
}

private fun edgeMid(direction: Direction, a: Float, b: Float, c: Float) = when (direction) {
    Direction.UP -> Offset(c, a)
    Direction.RIGHT -> Offset(b, c)
    Direction.DOWN -> Offset(c, b)
    Direction.LEFT -> Offset(a, c)
}

/**
 * START is a solid node, FINISH a target ring, so they differ by shape not only colour.
 * [pulse] in 0..1 drives an expanding halo.
 */
fun DrawScope.drawTerminal(palette: BoardPalette, isStart: Boolean, routeColor: Color, pulse: Float = 0f) {
    drawTileBody(palette.terminalFace, lerpColor(palette.terminalFace, Color.White, 0.18f), palette.terminalEdge, palette.tileShadow)
    val opening = if (isStart) Direction.RIGHT else Direction.LEFT
    val a = side * TILE_INSET
    val c = side / 2
    val edge = edgeMid(opening, a, side - a, c)
    drawLine(palette.terminalEdge, Offset(c, c), edge, side * GROOVE_WIDTH, StrokeCap.Butt)
    drawLine(routeColor, Offset(c, c), edge, side * LINE_WIDTH, StrokeCap.Butt)

    val center = Offset(c, c)
    if (pulse > 0f) {
        drawCircle(
            palette.terminalMark.copy(alpha = 0.55f * (1f - pulse)),
            radius = side * (0.17f + 0.2f * pulse),
            center = center,
            style = Stroke(side * 0.03f),
        )
    }
    if (isStart) {
        drawCircle(palette.terminalMark, radius = side * 0.15f, center = center)
        drawCircle(routeColor, radius = side * 0.06f, center = center)
    } else {
        drawCircle(palette.terminalEdge, radius = side * 0.17f, center = center)
        drawCircle(palette.terminalMark, radius = side * 0.15f, center = center, style = Stroke(side * 0.045f))
        drawCircle(palette.terminalMark, radius = side * 0.055f, center = center)
    }
}

/** Draws any board cell. */
fun DrawScope.drawPathCell(
    cell: PathCell,
    palette: BoardPalette,
    routeColor: Color,
    hint: Boolean = false,
    pulse: Float = 0f,
) {
    when (cell) {
        PathCell.Empty -> drawWell(palette, hint)
        PathCell.Blocked -> drawBlocked(palette)
        PathCell.Start -> { drawWell(palette); drawTerminal(palette, isStart = true, routeColor, pulse) }
        PathCell.Finish -> { drawWell(palette); drawTerminal(palette, isStart = false, routeColor, pulse) }
        is PathCell.Placed -> { drawWell(palette); drawTile(palette, cell.tile.connections, routeColor) }
    }
}

fun lerpColor(from: Color, to: Color, fraction: Float) = Color(
    red = from.red + (to.red - from.red) * fraction,
    green = from.green + (to.green - from.green) * fraction,
    blue = from.blue + (to.blue - from.blue) * fraction,
    alpha = from.alpha + (to.alpha - from.alpha) * fraction,
)
