package com.example.pathbuilder.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.game.Direction
import com.example.pathbuilder.game.PuzzleLayout
import com.example.pathbuilder.ui.components.PrimaryButton
import com.example.pathbuilder.ui.components.SecondaryButton
import com.example.pathbuilder.ui.components.StatBlock
import com.example.pathbuilder.ui.components.StatRow
import com.example.pathbuilder.ui.components.drawTerminal
import com.example.pathbuilder.ui.components.drawTile
import com.example.pathbuilder.ui.components.formatTime
import com.example.pathbuilder.ui.theme.LocalBoardPalette

@Composable
fun ResultScreen(result: GameResult, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
    val totalTiles = PuzzleLayout.Default.bagOrder.size

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.safeDrawingPadding().fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp)
                    .graphicsLayer {
                        alpha = enter.value
                        translationY = (1f - enter.value) * 16.dp.toPx()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ResultMark(result.success)
                Spacer(Modifier.height(28.dp))
                Text(
                    if (result.success) "ROUTE COMPLETE" else "ROUTE INCOMPLETE",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (result.success) "Start and finish are joined." else "The tiles ran out before the route closed.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))
                StatRow(Modifier.fillMaxWidth()) {
                    StatBlock("Moves", result.moves.toString(), Modifier.weight(1f))
                    if (result.success) {
                        StatBlock("Time", formatTime(result.timeMillis), Modifier.weight(1f))
                        StatBlock("Tiles Left", result.tilesRemaining.toString(), Modifier.weight(1f))
                        StatBlock("Path", result.pathLength.toString(), Modifier.weight(1f))
                    } else {
                        StatBlock("Tiles Used", (totalTiles - result.tilesRemaining).toString(), Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(36.dp))
                PrimaryButton(if (result.success) "Play Again" else "Try Again", onPlayAgain, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                SecondaryButton("Home", onHome, Modifier.fillMaxWidth())
            }
        }
    }
}

/** START — straight — FINISH. On failure the middle piece is turned so the route visibly breaks. */
@Composable
private fun ResultMark(success: Boolean) {
    val palette = LocalBoardPalette.current
    val color = if (success) palette.routeWin else palette.route
    Row(Modifier.clearAndSetSemantics { }) {
        Canvas(Modifier.size(56.dp)) { drawTerminal(palette, isStart = true, routeColor = color) }
        Canvas(Modifier.size(56.dp)) {
            drawTile(palette, if (success) setOf(Direction.LEFT, Direction.RIGHT) else setOf(Direction.UP, Direction.DOWN), color)
        }
        Canvas(Modifier.size(56.dp)) { drawTerminal(palette, isStart = false, routeColor = color) }
    }
}
