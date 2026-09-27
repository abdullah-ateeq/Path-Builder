package com.example.pathbuilder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pathbuilder.data.GameSettings
import com.example.pathbuilder.feedback.LocalGameFeedback
import com.example.pathbuilder.game.PathBuilderState
import com.example.pathbuilder.ui.components.GameBoard
import com.example.pathbuilder.ui.components.Glyph
import com.example.pathbuilder.ui.components.GlyphIcon
import com.example.pathbuilder.ui.components.PrimaryButton
import com.example.pathbuilder.ui.components.SecondaryButton
import com.example.pathbuilder.ui.components.StatBlock
import com.example.pathbuilder.ui.components.StatRow
import com.example.pathbuilder.ui.components.TrayTile
import com.example.pathbuilder.ui.components.formatTime
import kotlinx.coroutines.delay

private const val TRAY_SLOTS = 3
private const val RESULT_DELAY_MS = 1700L

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: GameSettings,
    onExit: () -> Unit,
    onFinished: (GameResult) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val elapsed by viewModel.elapsed.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val feedback = LocalGameFeedback.current
    var confirmRestart by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel, feedback) {
        viewModel.events.collect { feedback?.perform(it) }
    }
    LifecycleResumeEffect(viewModel) {
        viewModel.resumeTimer()
        onPauseOrDispose { viewModel.pauseTimer() }
    }
    LaunchedEffect(state.isOver) {
        if (state.isOver) {
            delay(RESULT_DELAY_MS)
            onFinished(viewModel.result())
        }
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("Restart this puzzle?") },
            text = { Text("The board and tile bag return to their starting state.") },
            confirmButton = {
                TextButton(onClick = { confirmRestart = false; viewModel.restart() }) { Text("Restart") }
            },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }

    val controls = Controls(
        onSelect = viewModel::selectTile,
        onRotate = viewModel::rotateSelected,
        onUndo = viewModel::undo,
        onRestart = { if (!state.isOver) confirmRestart = true },
        onExit = onExit,
    )

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.safeDrawingPadding()) {
            val landscape = maxWidth > maxHeight
            if (landscape) {
                Row(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BoardArea(state, settings, viewModel, Modifier.fillMaxHeight())
                    Column(
                        Modifier.widthIn(max = 380.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        TopBar(controls, state.isOver)
                        Hud(state, elapsed, settings.showTimer)
                        Tray(state, controls)
                        Actions(state, canUndo, controls)
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TopBar(controls, state.isOver)
                        Hud(state, elapsed, settings.showTimer)
                    }
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        BoardArea(state, settings, viewModel, Modifier.widthIn(max = 560.dp).padding(vertical = 12.dp))
                    }
                    Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Tray(state, controls)
                        Actions(state, canUndo, controls)
                    }
                }
            }
        }
    }
}

private class Controls(
    val onSelect: (Int) -> Unit,
    val onRotate: () -> Unit,
    val onUndo: () -> Unit,
    val onRestart: () -> Unit,
    val onExit: () -> Unit,
)

@Composable
private fun TopBar(controls: Controls, isOver: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = controls.onExit, modifier = Modifier.semantics { contentDescription = "Back to menu" }) {
            GlyphIcon(Glyph.BACK, MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "PATH BUILDER",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = controls.onRestart,
            enabled = !isOver,
            modifier = Modifier.semantics { contentDescription = "Restart puzzle" },
        ) {
            GlyphIcon(Glyph.RESTART, MaterialTheme.colorScheme.onBackground.copy(alpha = if (isOver) 0.3f else 1f))
        }
    }
}

@Composable
private fun Hud(state: PathBuilderState, elapsed: Long, showTimer: Boolean) {
    StatRow(Modifier.fillMaxWidth()) {
        StatBlock("Tiles Left", state.tilesRemaining.toString(), Modifier.weight(1f))
        StatBlock("Moves", state.moves.toString(), Modifier.weight(1f))
        if (showTimer) StatBlock("Time", formatTime(elapsed), Modifier.weight(1f))
    }
}

@Composable
private fun BoardArea(state: PathBuilderState, settings: GameSettings, viewModel: GameViewModel, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        GameBoard(state = state, showConnected = settings.showConnectedPath, onCellTap = viewModel::placeAt)
    }
}

/** Shown in place of the tray once the game ends, so the finished board stays fully visible. */
@Composable
private fun OutcomeBanner(complete: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Text(
            if (complete) "ROUTE COMPLETE" else "ROUTE INCOMPLETE",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 28.dp),
        )
    }
}

@Composable
private fun Tray(state: PathBuilderState, controls: Controls) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TILES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(
                hint(state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        AnimatedVisibility(
            visible = state.isOver,
            enter = fadeIn(tween(260)) + scaleIn(tween(260), initialScale = 0.92f),
            exit = fadeOut(tween(120)),
        ) {
            OutcomeBanner(state.isComplete, Modifier.fillMaxWidth())
        }
        if (!state.isOver) Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            repeat(TRAY_SLOTS) { index ->
                val tile = state.tray.getOrNull(index)
                TrayTile(
                    tile = tile,
                    selected = tile != null && tile.id == state.selectedTileId,
                    enabled = !state.isOver,
                    onClick = { tile?.let { controls.onSelect(it.id) } },
                    modifier = Modifier.weight(1f).widthIn(max = 96.dp),
                )
            }
        }
    }
}

private fun hint(state: PathBuilderState): String = when {
    state.isComplete -> "Route complete"
    state.isFailed -> "Out of tiles"
    state.selectedTile != null -> "Tap an empty cell"
    state.tray.isEmpty() -> "No tiles left"
    else -> "Choose a tile"
}

@Composable
private fun Actions(state: PathBuilderState, canUndo: Boolean, controls: Controls) {
    Row(Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton(
            text = "Rotate",
            onClick = controls.onRotate,
            enabled = state.selectedTile != null && !state.isOver,
            modifier = Modifier.weight(1.4f),
        )
        SecondaryButton(
            text = "Undo",
            onClick = controls.onUndo,
            enabled = canUndo,
            modifier = Modifier.weight(1f).height(56.dp),
            leading = {
                GlyphIcon(
                    Glyph.UNDO,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = if (canUndo) 1f else 0.38f),
                    size = 18.dp,
                )
            },
        )
    }
}
