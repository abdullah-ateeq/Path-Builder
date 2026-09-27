package com.example.pathbuilder.game

data class PathBuilderState(
    val board: Board,
    val tray: List<RouteTile>,
    val bag: List<RouteTile>,
    val selectedTileId: Int? = null,
    val moves: Int = 0,
    val elapsedMillis: Long = 0L,
    val winningPath: List<GridPosition> = emptyList(),
    val connectedCells: Set<GridPosition> = emptySet(),
    val isComplete: Boolean = false,
    val isFailed: Boolean = false,
    val isAnimating: Boolean = false,
) {
    val selectedTile: RouteTile? get() = tray.firstOrNull { it.id == selectedTileId }
    val tilesRemaining: Int get() = tray.size + bag.size
    val isOver: Boolean get() = isComplete || isFailed

    /** Placed tiles on the winning route, excluding START and FINISH. */
    val pathLength: Int get() = (winningPath.size - 2).coerceAtLeast(0)
}

enum class RejectReason { NO_TILE_SELECTED, OUT_OF_BOUNDS, BLOCKED, OCCUPIED, START_OR_FINISH, GAME_OVER, BUSY }

sealed interface PlacementResult {
    /** Tile placed but it does not touch the route growing from START. */
    data object Placed : PlacementResult

    /** Tile placed and joined the route growing from START. */
    data object Connected : PlacementResult
    data object Completed : PlacementResult
    data object Failed : PlacementResult
    data class Rejected(val reason: RejectReason) : PlacementResult
}

/**
 * Pure game rules. Holds the current [state] plus full snapshots for undo.
 * Contains no Android or UI code so it can be unit tested directly.
 */
class PathBuilderGame(private val layout: PuzzleLayout = PuzzleLayout.Default) {

    var state: PathBuilderState = initialState()
        private set

    private val history = ArrayList<PathBuilderState>()

    val canUndo: Boolean get() = history.isNotEmpty() && !state.isOver && !state.isAnimating

    private fun initialState(): PathBuilderState {
        val tiles = layout.initialTiles()
        val board = layout.initialBoard()
        return PathBuilderState(
            board = board,
            tray = tiles.take(layout.traySize),
            bag = tiles.drop(layout.traySize),
            connectedCells = PathFinder.connectedFromStart(board),
        )
    }

    /** Choosing and turning tray tiles stays responsive during the brief placement animation; placing does not. */
    private val acceptsInput: Boolean get() = !state.isOver

    /** Selects a tray tile, or clears the selection when the already-selected tile is tapped. */
    fun select(tileId: Int): Boolean {
        if (!acceptsInput || state.tray.none { it.id == tileId }) return false
        state = state.copy(selectedTileId = if (state.selectedTileId == tileId) null else tileId)
        return true
    }

    fun rotateSelected(): Boolean {
        if (!acceptsInput) return false
        val selected = state.selectedTile ?: return false
        state = state.copy(tray = state.tray.map { if (it.id == selected.id) it.rotatedClockwise() else it })
        return true
    }

    fun place(position: GridPosition): PlacementResult {
        if (state.isOver) return PlacementResult.Rejected(RejectReason.GAME_OVER)
        if (state.isAnimating) return PlacementResult.Rejected(RejectReason.BUSY)
        val tile = state.selectedTile ?: return PlacementResult.Rejected(RejectReason.NO_TILE_SELECTED)
        if (!isInside(position, state.board.size)) return PlacementResult.Rejected(RejectReason.OUT_OF_BOUNDS)
        when (state.board.cellAt(position)) {
            PathCell.Blocked -> return PlacementResult.Rejected(RejectReason.BLOCKED)
            PathCell.Start, PathCell.Finish -> return PlacementResult.Rejected(RejectReason.START_OR_FINISH)
            is PathCell.Placed -> return PlacementResult.Rejected(RejectReason.OCCUPIED)
            PathCell.Empty -> Unit
        }

        history += state

        val board = state.board.with(position, PathCell.Placed(tile))
        val slot = state.tray.indexOfFirst { it.id == tile.id }
        val next = state.bag.firstOrNull()
        val tray = state.tray.toMutableList().apply {
            if (next != null) set(slot, next) else removeAt(slot)
        }
        val bag = if (next != null) state.bag.drop(1) else state.bag
        val winningPath = PathFinder.findWinningPath(board)
        val connected = PathFinder.connectedFromStart(board)
        val complete = winningPath != null
        val failed = !complete && tray.isEmpty() && bag.isEmpty()

        state = state.copy(
            board = board,
            tray = tray,
            bag = bag,
            selectedTileId = null,
            moves = state.moves + 1,
            winningPath = winningPath.orEmpty(),
            connectedCells = connected,
            isComplete = complete,
            isFailed = failed,
        )

        return when {
            complete -> PlacementResult.Completed
            failed -> PlacementResult.Failed
            position in connected -> PlacementResult.Connected
            else -> PlacementResult.Placed
        }
    }

    /** Restores the snapshot taken before the last placement. Elapsed time keeps running. */
    fun undo(): Boolean {
        if (!canUndo) return false
        val previous = history.removeAt(history.lastIndex)
        state = previous.copy(elapsedMillis = state.elapsedMillis, isAnimating = false)
        return true
    }

    fun restart() {
        history.clear()
        state = initialState()
    }

    fun setAnimating(animating: Boolean) {
        state = state.copy(isAnimating = animating)
    }

    fun updateElapsed(millis: Long) {
        state = state.copy(elapsedMillis = millis)
    }
}
