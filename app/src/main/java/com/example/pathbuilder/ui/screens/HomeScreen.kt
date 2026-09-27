package com.example.pathbuilder.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.data.GameStats
import com.example.pathbuilder.game.Direction
import com.example.pathbuilder.ui.components.PrimaryButton
import com.example.pathbuilder.ui.components.QuietButton
import com.example.pathbuilder.ui.components.StatBlock
import com.example.pathbuilder.ui.components.StatRow
import com.example.pathbuilder.ui.components.drawTile
import com.example.pathbuilder.ui.components.formatTime
import com.example.pathbuilder.ui.theme.LocalBoardPalette

@Composable
fun HomeScreen(
    stats: GameStats,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.safeDrawingPadding().fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Emblem()
                Spacer(Modifier.height(28.dp))
                Text(
                    "PATH BUILDER",
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Place. Connect. Complete the route.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(40.dp))
                PrimaryButton("Play", onPlay, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    QuietButton("How to Play", onHowToPlay)
                    QuietButton("Settings", onSettings)
                    QuietButton("About", onAbout)
                }
                Spacer(Modifier.height(32.dp))
                StatRow(Modifier.fillMaxWidth()) {
                    StatBlock("Played", stats.gamesPlayed.toString(), Modifier.weight(1f))
                    StatBlock("Routes", stats.routesCompleted.toString(), Modifier.weight(1f))
                    StatBlock("Best Moves", stats.bestMoves?.toString() ?: "—", Modifier.weight(1f))
                    StatBlock("Best Time", stats.bestTimeMillis?.let(::formatTime) ?: "—", Modifier.weight(1f))
                }
            }
        }
    }
}

/** Four corner pieces closing into a loop — the app's mark. */
@Composable
private fun Emblem() {
    val palette = LocalBoardPalette.current
    val loop = listOf(
        listOf(setOf(Direction.RIGHT, Direction.DOWN), setOf(Direction.DOWN, Direction.LEFT)),
        listOf(setOf(Direction.UP, Direction.RIGHT), setOf(Direction.LEFT, Direction.UP)),
    )
    Column(Modifier.clearAndSetSemantics { }) {
        loop.forEach { row ->
            Row {
                row.forEach { openings ->
                    Canvas(Modifier.size(52.dp)) { drawTile(palette, openings, palette.routeConnected) }
                }
            }
        }
    }
}
