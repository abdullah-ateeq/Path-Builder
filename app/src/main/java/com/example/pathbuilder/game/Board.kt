package com.example.pathbuilder.game

const val BOARD_SIZE = 6

data class GridPosition(val row: Int, val col: Int)

sealed class PathCell {
    data object Empty : PathCell()
    data object Blocked : PathCell()
    data object Start : PathCell()
    data object Finish : PathCell()
    data class Placed(val tile: RouteTile) : PathCell()
}

typealias Board = List<List<PathCell>>

fun Board.cellAt(position: GridPosition): PathCell = this[position.row][position.col]

fun Board.with(position: GridPosition, cell: PathCell): Board =
    mapIndexed { r, row ->
        if (r != position.row) row else row.mapIndexed { c, old -> if (c == position.col) cell else old }
    }

fun Board.positionOf(target: PathCell): GridPosition? {
    forEachIndexed { r, row ->
        row.forEachIndexed { c, cell -> if (cell == target) return GridPosition(r, c) }
    }
    return null
}

fun isInside(position: GridPosition, size: Int = BOARD_SIZE): Boolean =
    position.row in 0 until size && position.col in 0 until size

/** The adjacent position in [direction], or null when it would fall outside the board. */
fun neighbor(position: GridPosition, direction: Direction, size: Int = BOARD_SIZE): GridPosition? {
    val next = GridPosition(position.row + direction.rowDelta, position.col + direction.colDelta)
    return if (isInside(next, size)) next else null
}

/** START opens only RIGHT, FINISH opens only LEFT, placed tiles open per their rotation. */
fun PathCell.openings(): Set<Direction> = when (this) {
    PathCell.Start -> setOf(Direction.RIGHT)
    PathCell.Finish -> setOf(Direction.LEFT)
    is PathCell.Placed -> tile.connections
    PathCell.Empty, PathCell.Blocked -> emptySet()
}

fun PathCell.describe(position: GridPosition): String {
    val where = "row ${position.row + 1} column ${position.col + 1}"
    return when (this) {
        PathCell.Empty -> "Empty cell $where"
        PathCell.Blocked -> "Blocked cell $where"
        PathCell.Start -> "Start cell, opens right"
        PathCell.Finish -> "Finish cell, opens left"
        is PathCell.Placed -> "${tile.describe()}, $where"
    }
}
