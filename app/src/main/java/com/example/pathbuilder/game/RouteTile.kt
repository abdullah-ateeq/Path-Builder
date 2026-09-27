package com.example.pathbuilder.game

/**
 * Tile shapes. Each kind defines its openings at rotation 0:
 * a straight runs LEFT–RIGHT, a corner joins UP–RIGHT.
 */
enum class TileKind(val baseConnections: Set<Direction>, val label: String) {
    STRAIGHT(setOf(Direction.LEFT, Direction.RIGHT), "straight"),
    CORNER(setOf(Direction.UP, Direction.RIGHT), "corner"),
}

data class RouteTile(
    val id: Int,
    val kind: TileKind,
    val rotation: Int = 0,
) {
    init {
        require(rotation in VALID_ROTATIONS) { "Rotation must be one of $VALID_ROTATIONS, was $rotation" }
    }

    /** Logical openings, always derived from [kind] and [rotation] so they can never drift out of sync. */
    val connections: Set<Direction> get() = getConnections(this)

    fun rotatedClockwise(): RouteTile = copy(rotation = (rotation + 90) % 360)

    companion object {
        val VALID_ROTATIONS = setOf(0, 90, 180, 270)
    }
}

fun getConnections(tile: RouteTile): Set<Direction> {
    var directions = tile.kind.baseConnections
    repeat(tile.rotation / 90) {
        directions = directions.mapTo(mutableSetOf()) { it.rotatedClockwise() }
    }
    return directions
}

/** e.g. "Corner tile connecting up and right". */
fun RouteTile.describe(): String {
    val sides = connections.sortedBy { it.ordinal }.joinToString(" and ") { it.label }
    return "${kind.label.replaceFirstChar { it.uppercase() }} tile connecting $sides"
}
