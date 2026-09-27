package com.example.pathbuilder.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.game.Direction
import com.example.pathbuilder.game.PathCell
import com.example.pathbuilder.game.RouteTile
import com.example.pathbuilder.game.TileKind
import com.example.pathbuilder.ui.components.PageScaffold
import com.example.pathbuilder.ui.components.drawPathCell
import com.example.pathbuilder.ui.theme.LocalBoardPalette
import com.example.pathbuilder.ui.theme.NumberStyle

private val rules = listOf(
    "Choose a route tile from the tray.",
    "Tap Rotate to turn it 90° clockwise.",
    "Tap an empty cell to place it. Placed tiles stay put — use Undo to take one back.",
    "Neighbouring openings must meet: a right opening joins only a left opening, and so on.",
    "Build one continuous route from START on the left to FINISH on the right.",
    "Hatched cells are blocked and cannot hold a tile.",
    "Complete the route before the tiles run out.",
)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    PageScaffold("How to Play", onBack) {
        rules.forEachIndexed { index, rule ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    "%02d".format(index + 1),
                    style = NumberStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.width(40.dp),
                )
                Text(rule, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Text("CONNECTED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        ExampleStrip(
            cells = listOf(
                PathCell.Start,
                PathCell.Placed(RouteTile(1, TileKind.STRAIGHT)),
                PathCell.Placed(RouteTile(2, TileKind.CORNER, 180)),
            ),
            connected = true,
            description = "Example: start, then a horizontal straight, then a corner connecting left and down. All openings meet.",
        )
        Text("NOT CONNECTED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        ExampleStrip(
            cells = listOf(
                PathCell.Start,
                PathCell.Placed(RouteTile(3, TileKind.STRAIGHT, 90)),
                PathCell.Blocked,
            ),
            connected = false,
            description = "Example: start next to a vertical straight. The start opens right but the tile has no left opening, so they do not connect. A hatched blocked cell follows.",
        )
        Text(
            "The line lights up teal while it is joined to START, so you can see how far the route reaches.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExampleStrip(cells: List<PathCell>, connected: Boolean, description: String) {
    val palette = LocalBoardPalette.current
    Row(
        Modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val color = if (connected) palette.routeConnected else palette.route
        cells.forEach { cell ->
            Canvas(Modifier.size(64.dp)) { drawPathCell(cell, palette, color) }
        }
    }
}
