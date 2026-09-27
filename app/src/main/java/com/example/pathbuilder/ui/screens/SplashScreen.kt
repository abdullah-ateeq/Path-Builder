package com.example.pathbuilder.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.game.Direction
import com.example.pathbuilder.game.Direction.DOWN
import com.example.pathbuilder.game.Direction.LEFT
import com.example.pathbuilder.game.Direction.RIGHT
import com.example.pathbuilder.game.Direction.UP
import com.example.pathbuilder.ui.components.drawTerminal
import com.example.pathbuilder.ui.components.drawTile
import com.example.pathbuilder.ui.components.drawWell
import com.example.pathbuilder.ui.components.lerpColor
import com.example.pathbuilder.ui.theme.BoardPalette
import com.example.pathbuilder.ui.theme.LocalBoardPalette
import kotlin.math.sin

private const val SPLASH_MS = 1500

private const val COLS = 5
private const val ROWS = 3

/** Route pieces in path order: (row, col, openings). */
private val splashRoute: List<Triple<Int, Int, Set<Direction>>> = listOf(
    Triple(1, 1, setOf(LEFT, UP)),
    Triple(0, 1, setOf(DOWN, RIGHT)),
    Triple(0, 2, setOf(LEFT, RIGHT)),
    Triple(0, 3, setOf(LEFT, DOWN)),
    Triple(1, 3, setOf(UP, RIGHT)),
)

/**
 * Timeline over [SPLASH_MS]: grid fades in (0–20%), tiles drop in (15–55%),
 * route lights up piece by piece (50–80%), terminals pulse, title fades in (55–85%).
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val palette = LocalBoardPalette.current
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SPLASH_MS, easing = LinearEasing))
        onFinished()
    }
    val p = progress.value

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SplashBoard(palette, p, Modifier.widthIn(max = 300.dp).fillMaxWidth().aspectRatio(COLS / ROWS.toFloat()))
            Box(
                Modifier.graphicsLayer {
                    alpha = phase(p, 0.55f, 0.85f)
                    translationY = (1f - phase(p, 0.55f, 0.85f)) * 12.dp.toPx()
                },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PATH BUILDER", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

private fun phase(p: Float, start: Float, end: Float) = ((p - start) / (end - start)).coerceIn(0f, 1f)

@Composable
private fun SplashBoard(palette: BoardPalette, p: Float, modifier: Modifier) {
    val gridAlpha = phase(p, 0f, 0.2f)
    val lit = phase(p, 0.5f, 0.8f) * (splashRoute.size + 2)
    val pulse = if (p > 0.8f) sin(phase(p, 0.8f, 1f) * Math.PI.toFloat()) else 0f
    fun routeColor(index: Int) = lerpColor(palette.route, palette.routeConnected, (lit - index).coerceIn(0f, 1f))

    Column(modifier) {
        for (r in 0 until ROWS) {
            Row(Modifier.weight(1f)) {
                for (c in 0 until COLS) {
                    val routeIndex = splashRoute.indexOfFirst { it.first == r && it.second == c }
                    val terminal = r == 1 && (c == 0 || c == COLS - 1)
                    val appear = when {
                        terminal -> phase(p, 0.1f, 0.25f)
                        routeIndex >= 0 -> phase(p, 0.15f + routeIndex * 0.07f, 0.27f + routeIndex * 0.07f)
                        else -> 0f
                    }
                    Box(Modifier.weight(1f).fillMaxHeight().graphicsLayer { alpha = gridAlpha }.drawBehind { drawWell(palette) }) {
                        if (appear > 0f) {
                            Canvas(
                                Modifier.fillMaxSize().graphicsLayer {
                                    alpha = appear
                                    scaleX = 0.7f + 0.3f * appear
                                    scaleY = 0.7f + 0.3f * appear
                                },
                            ) {
                                when {
                                    terminal && c == 0 -> drawTerminal(palette, isStart = true, routeColor(0), pulse)
                                    terminal -> drawTerminal(palette, isStart = false, routeColor(splashRoute.size + 1), pulse)
                                    else -> drawTile(palette, splashRoute[routeIndex].third, routeColor(routeIndex + 1))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
