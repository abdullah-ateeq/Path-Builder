package com.example.pathbuilder.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.pathbuilder.ui.components.PageScaffold

private val features = listOf(
    "6×6 Puzzle Board",
    "Tile Rotation",
    "Route Connectivity",
    "Offline Play",
    "Premium Themes",
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    PageScaffold("About", onBack) {
        Column {
            Text("Path Builder", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("Version 1.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "A polished route-building puzzle where players place and rotate tiles to create a continuous path from start to finish.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val accent = MaterialTheme.colorScheme.secondary
            features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(10.dp)) {
                        val w = size.width
                        drawPath(
                            Path().apply {
                                moveTo(w / 2, 0f); lineTo(w, w / 2); lineTo(w / 2, w); lineTo(0f, w / 2); close()
                            },
                            accent,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(feature, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
