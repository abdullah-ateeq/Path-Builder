package com.example.pathbuilder.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.game.GridPosition
import com.example.pathbuilder.game.PathBuilderState
import com.example.pathbuilder.game.PathCell
import com.example.pathbuilder.game.RouteTile
import com.example.pathbuilder.game.describe
import com.example.pathbuilder.ui.theme.BoardPalette
import com.example.pathbuilder.ui.theme.LocalBoardPalette

@Composable
fun GameBoard(
    state: PathBuilderState,
    showConnected: Boolean,
    onCellTap: (GridPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoardPalette.current
    val pulse = if (state.isComplete) {
        val transition = rememberInfiniteTransition(label = "win")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
            label = "pulse",
        ).value
    } else 0f
    val winning = remember(state.winningPath) { state.winningPath.toSet() }
    val hasRoute = state.connectedCells.size > 1
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier
            .aspectRatio(1f)
            .shadow(18.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .background(palette.frame, shape)
            .border(1.dp, palette.frameEdge, shape)
            .padding(8.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            state.board.forEachIndexed { r, row ->
                Row(Modifier.weight(1f)) {
                    row.forEachIndexed { c, cell ->
                        val position = GridPosition(r, c)
                        val routeColor = when {
                            position in winning -> palette.routeWin
                            showConnected && hasRoute && position in state.connectedCells -> palette.routeConnected
                            else -> palette.route
                        }
                        BoardCell(
                            cell = cell,
                            position = position,
                            palette = palette,
                            routeColor = routeColor,
                            onRoute = position in winning || (hasRoute && position in state.connectedCells),
                            hint = cell == PathCell.Empty && state.selectedTile != null && !state.isOver,
                            pulse = if (cell == PathCell.Start || cell == PathCell.Finish) pulse else 0f,
                            onTap = { onCellTap(position) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    cell: PathCell,
    position: GridPosition,
    palette: BoardPalette,
    routeColor: Color,
    onRoute: Boolean,
    hint: Boolean,
    pulse: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(routeColor, tween(280), label = "route")
    val tileId = (cell as? PathCell.Placed)?.tile?.id
    val appear = remember(tileId) { Animatable(if (tileId != null) 0f else 1f) }
    LaunchedEffect(tileId) {
        if (tileId != null) appear.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
    }
    val description = cell.describe(position) + if (onRoute && cell is PathCell.Placed) ", on route" else ""

    Box(
        modifier
            .semantics {
                contentDescription = description
                if (cell == PathCell.Empty) role = Role.Button
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            )
            .drawBehind {
                if (cell == PathCell.Blocked) drawBlocked(palette) else drawWell(palette, hint)
            },
    ) {
        when (cell) {
            is PathCell.Placed -> Canvas(
                Modifier.fillMaxSize().graphicsLayer {
                    val s = 0.6f + 0.4f * appear.value
                    scaleX = s
                    scaleY = s
                    alpha = appear.value
                },
            ) { drawTile(palette, cell.tile.connections, color) }
            PathCell.Start, PathCell.Finish -> Canvas(Modifier.fillMaxSize()) {
                drawTerminal(palette, isStart = cell == PathCell.Start, routeColor = color, pulse = pulse)
            }
            PathCell.Empty, PathCell.Blocked -> Unit
        }
    }
}

/** A tray piece. Lifts and gains an accent border when selected; rotation animates clockwise. */
@Composable
fun TrayTile(
    tile: RouteTile?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoardPalette.current
    val lift by animateDpAsState(if (selected) (-6).dp else 0.dp, spring(dampingRatio = 0.6f), label = "lift")
    val borderAlpha by animateFloatAsState(if (selected) 1f else 0f, tween(160), label = "border")

    // Accumulate turns so 270° → 0° keeps spinning clockwise instead of snapping back.
    val tracker = remember(tile?.id) { RotationTracker(tile?.rotation ?: 0) }
    val rotation by animateFloatAsState(
        tracker.degreesFor(tile?.rotation ?: 0),
        tween(170, easing = FastOutSlowInEasing),
        label = "rotate",
    )

    val description = when {
        tile == null -> "Empty tray slot"
        selected -> "Selected ${tile.describe().replaceFirstChar { it.lowercase() }}"
        else -> tile.describe()
    }

    Box(
        modifier
            .aspectRatio(1f)
            .graphicsLayer { translationY = lift.toPx() }
            .semantics {
                contentDescription = description
                this.selected = selected
                if (tile != null) role = Role.Button
            }
            .clickable(
                enabled = enabled && tile != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (tile == null) {
                val inset = size.minDimension * 0.06f
                drawRoundRect(
                    palette.frameEdge,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(size.minDimension * 0.16f),
                    style = Stroke(width = 1.dp.toPx()),
                )
                return@Canvas
            }
            drawTile(
                palette = palette,
                connections = tile.kind.baseConnections,
                routeColor = if (selected) palette.routeConnected else palette.route,
                elevation = if (selected) 2.4f else 1f,
                rotation = rotation,
            )
            if (borderAlpha > 0f) {
                val inset = size.minDimension * 0.015f
                drawRoundRect(
                    palette.selection.copy(alpha = borderAlpha),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(size.minDimension * 0.2f),
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            }
        }
    }
}

/** Maps normalized 0–270° rotations onto an ever-increasing angle. Idempotent for repeated input. */
private class RotationTracker(private var last: Int) {
    private var total = last.toFloat()

    fun degreesFor(rotation: Int): Float {
        if (rotation != last) {
            total += ((rotation - last + 360) % 360).toFloat()
            last = rotation
        }
        return total
    }
}
