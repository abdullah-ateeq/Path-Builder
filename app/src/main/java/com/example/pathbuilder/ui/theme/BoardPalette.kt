package com.example.pathbuilder.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.pathbuilder.data.BoardTheme

/** Materials used to draw the board and route tiles. */
@Immutable
data class BoardPalette(
    val frame: Color,
    val frameEdge: Color,
    val well: Color,
    val wellEdge: Color,
    val hint: Color,
    val blocked: Color,
    val blockedHatch: Color,
    val tileLight: Color,
    val tileFace: Color,
    val tileEdge: Color,
    val tileShadow: Color,
    val groove: Color,
    val route: Color,
    val routeConnected: Color,
    val routeWin: Color,
    val terminalFace: Color,
    val terminalEdge: Color,
    val terminalMark: Color,
    val selection: Color,
)

val ClassicBoard = BoardPalette(
    frame = Color(0xFF3A3E44),
    frameEdge = Color(0xFF4A4F57),
    well = Color(0xFF2B2E33),
    wellEdge = Color(0xFF24272B),
    hint = Color(0x55EDE6D6),
    blocked = Color(0xFF1D1F23),
    blockedHatch = Color(0xFF41454C),
    tileLight = Color(0xFFF8F3E8),
    tileFace = Color(0xFFE9E1CF),
    tileEdge = Color(0xFFBDB29A),
    tileShadow = Color(0x66000000),
    groove = Color(0xFFD3C8B0),
    route = Color(0xFF9C7446),
    routeConnected = Color(0xFF3F8C82),
    routeWin = Color(0xFF2F9E8F),
    terminalFace = Color(0xFF55493B),
    terminalEdge = Color(0xFF3B3229),
    terminalMark = Color(0xFFE7C99A),
    selection = Color(0xFF5FA79D),
)

val MidnightBoard = BoardPalette(
    frame = Color(0xFF151B24),
    frameEdge = Color(0xFF222B37),
    well = Color(0xFF0E131A),
    wellEdge = Color(0xFF0A0E13),
    hint = Color(0x55C8D2DC),
    blocked = Color(0xFF07090C),
    blockedHatch = Color(0xFF1E2733),
    tileLight = Color(0xFF4A5566),
    tileFace = Color(0xFF323B48),
    tileEdge = Color(0xFF1E252F),
    tileShadow = Color(0x88000000),
    groove = Color(0xFF232A34),
    route = Color(0xFFCDB98E),
    routeConnected = Color(0xFF6CC1B4),
    routeWin = Color(0xFF86DCCD),
    terminalFace = Color(0xFF2B3A40),
    terminalEdge = Color(0xFF1A2428),
    terminalMark = Color(0xFFE6D5AE),
    selection = Color(0xFF6CC1B4),
)

fun BoardTheme.palette(): BoardPalette = when (this) {
    BoardTheme.CLASSIC -> ClassicBoard
    BoardTheme.MIDNIGHT -> MidnightBoard
}

val LocalBoardPalette = staticCompositionLocalOf { ClassicBoard }
