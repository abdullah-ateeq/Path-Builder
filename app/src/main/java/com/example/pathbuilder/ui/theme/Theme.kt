package com.example.pathbuilder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.pathbuilder.data.BoardTheme
import com.example.pathbuilder.data.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Graphite950,
    primaryContainer = TealDeep,
    onPrimaryContainer = Ivory,
    secondary = Bronze,
    onSecondary = Graphite950,
    tertiary = Clay,
    background = Graphite900,
    onBackground = Ivory,
    surface = Graphite900,
    onSurface = Ivory,
    surfaceVariant = Graphite800,
    onSurfaceVariant = Graphite400,
    surfaceContainer = Graphite850,
    surfaceContainerHigh = Graphite800,
    surfaceContainerHighest = Graphite700,
    outline = Graphite700,
    outlineVariant = Graphite800,
)

private val LightColors = lightColorScheme(
    primary = TealDeep,
    onPrimary = Stone50,
    primaryContainer = Teal,
    onPrimaryContainer = Ink,
    secondary = BronzeDeep,
    onSecondary = Stone50,
    tertiary = Clay,
    background = Stone100,
    onBackground = Ink,
    surface = Stone100,
    onSurface = Ink,
    surfaceVariant = Stone200,
    onSurfaceVariant = Stone600,
    surfaceContainer = Stone50,
    surfaceContainerHigh = Stone200,
    surfaceContainerHighest = Stone300,
    outline = Stone300,
    outlineVariant = Stone200,
)

@Composable
fun isAppInDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun PathBuilderTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    content: @Composable () -> Unit,
) {
    val colors = if (isAppInDarkTheme(themeMode)) DarkColors else LightColors
    CompositionLocalProvider(LocalBoardPalette provides boardTheme.palette()) {
        MaterialTheme(colorScheme = colors, typography = Typography, content = content)
    }
}
