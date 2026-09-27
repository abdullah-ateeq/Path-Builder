package com.example.pathbuilder.game

object PathFinder {

    /**
     * The neighbour reached by leaving [from] toward [direction], or null unless [from] opens that way
     * and the neighbour opens back the opposite way.
     */
    fun connectedNeighbor(board: Board, from: GridPosition, direction: Direction): GridPosition? {
        if (direction !in board.cellAt(from).openings()) return null
        val next = neighbor(from, direction, board.size) ?: return null
        return if (opposite(direction) in board.cellAt(next).openings()) next else null
    }

    /** BFS from START through matching openings. Returns START..FINISH inclusive, or null. */
    fun findWinningPath(board: Board): List<GridPosition>? {
        val start = board.positionOf(PathCell.Start) ?: return null
        val finish = board.positionOf(PathCell.Finish) ?: return null
        val parents = bfs(board, start)
        if (finish !in parents) return null
        val path = ArrayList<GridPosition>()
        var current: GridPosition? = finish
        while (current != null) {
            path += current
            current = parents[current]
        }
        return path.asReversed()
    }

    /** Every cell reachable from START, including START itself. Used for the live route highlight. */
    fun connectedFromStart(board: Board): Set<GridPosition> {
        val start = board.positionOf(PathCell.Start) ?: return emptySet()
        return bfs(board, start).keys
    }

    private fun bfs(board: Board, origin: GridPosition): Map<GridPosition, GridPosition?> {
        val parents = linkedMapOf<GridPosition, GridPosition?>(origin to null)
        val queue = ArrayDeque(listOf(origin))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (direction in Direction.entries) {
                val next = connectedNeighbor(board, current, direction) ?: continue
                if (next !in parents) {
                    parents[next] = current
                    queue.addLast(next)
                }
            }
        }
        return parents
    }
}
