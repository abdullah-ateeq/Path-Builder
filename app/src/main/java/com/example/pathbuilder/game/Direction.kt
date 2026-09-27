package com.example.pathbuilder.game

enum class Direction(val rowDelta: Int, val colDelta: Int) {
    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1);

    /** The direction after a 90° clockwise turn: UP → RIGHT → DOWN → LEFT → UP. */
    fun rotatedClockwise(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    val label: String get() = name.lowercase()
}

fun opposite(direction: Direction): Direction = when (direction) {
    Direction.UP -> Direction.DOWN
    Direction.RIGHT -> Direction.LEFT
    Direction.DOWN -> Direction.UP
    Direction.LEFT -> Direction.RIGHT
}
