package com.example.pathbuilder.game

/**
 * The handcrafted puzzle.
 *
 * ```
 *    c0 c1 c2 c3 c4 c5
 * r0  .  .  .  .  .  .
 * r1  .  .  .  X  .  .
 * r2  S  .  X  .  .  .
 * r3  .  .  X  .  .  F
 * r4  .  .  .  .  X  .
 * r5  X  .  .  .  .  .
 * ```
 *
 * Shortest route (7 tiles: 4 corners, 3 straights) dips under the wall at c2 and climbs back
 * before the block at (4,4). A 9-tile route over the top also works. The bag holds 6 of each kind
 * in a fixed order so every player receives enough useful pieces.
 */
data class PuzzleLayout(
    val start: GridPosition,
    val finish: GridPosition,
    val blocked: Set<GridPosition>,
    val bagOrder: List<TileKind>,
    val traySize: Int = 3,
) {
    fun initialBoard(): Board = List(BOARD_SIZE) { r ->
        List(BOARD_SIZE) { c ->
            val p = GridPosition(r, c)
            when (p) {
                start -> PathCell.Start
                finish -> PathCell.Finish
                in blocked -> PathCell.Blocked
                else -> PathCell.Empty
            }
        }
    }

    fun initialTiles(): List<RouteTile> = bagOrder.mapIndexed { index, kind -> RouteTile(id = index + 1, kind = kind) }

    companion object {
        private val C = TileKind.CORNER
        private val S = TileKind.STRAIGHT

        val Default = PuzzleLayout(
            start = GridPosition(2, 0),
            finish = GridPosition(3, 5),
            blocked = setOf(
                GridPosition(1, 3),
                GridPosition(2, 2),
                GridPosition(3, 2),
                GridPosition(4, 4),
                GridPosition(5, 0),
            ),
            bagOrder = listOf(C, S, C, S, C, S, S, C, C, S, C, S),
        )
    }
}
