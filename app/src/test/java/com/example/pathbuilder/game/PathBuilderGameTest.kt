package com.example.pathbuilder.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private val C = TileKind.CORNER
private val S = TileKind.STRAIGHT

private fun tile(kind: TileKind, rotation: Int = 0, id: Int = 0) = RouteTile(id, kind, rotation)
private fun pos(r: Int, c: Int) = GridPosition(r, c)

private fun boardWith(vararg tiles: Pair<GridPosition, RouteTile>): Board =
    tiles.fold(PuzzleLayout.Default.initialBoard()) { b, (p, t) -> b.with(p, PathCell.Placed(t)) }

/** The 7-tile solution for the default layout. */
private val solution = listOf(
    pos(2, 1) to tile(C, 180), // left + down
    pos(3, 1) to tile(S, 90),  // vertical
    pos(4, 1) to tile(C, 0),   // up + right
    pos(4, 2) to tile(S, 0),   // horizontal
    pos(4, 3) to tile(C, 270), // left + up
    pos(3, 3) to tile(C, 90),  // right + down
    pos(3, 4) to tile(S, 0),   // horizontal
)

/** Selects a tray tile of [kind], rotates it to [rotation] and places it. */
private fun PathBuilderGame.play(position: GridPosition, kind: TileKind, rotation: Int): PlacementResult {
    val t = state.tray.first { it.kind == kind }
    assertTrue(select(t.id))
    repeat(rotation / 90) { assertTrue(rotateSelected()) }
    return place(position)
}

/** Spends every tile on edge cells that never touch START. */
private fun PathBuilderGame.wasteAllTiles(): PlacementResult {
    val cells = (0..5).map { pos(0, it) } + (1..5).map { pos(5, it) } + pos(1, 5)
    var last: PlacementResult = PlacementResult.Placed
    for (cell in cells) {
        if (state.tilesRemaining == 0) break
        assertTrue(select(state.tray.first().id))
        last = place(cell)
    }
    return last
}

class RotationTest {
    @Test fun straightRotatesBetweenHorizontalAndVertical() {
        assertEquals(setOf(Direction.LEFT, Direction.RIGHT), tile(S).connections)
        assertEquals(setOf(Direction.UP, Direction.DOWN), tile(S).rotatedClockwise().connections)
    }

    @Test fun cornerRotatesClockwiseThroughAllFour() {
        val t0 = tile(C)
        val t90 = t0.rotatedClockwise()
        val t180 = t90.rotatedClockwise()
        val t270 = t180.rotatedClockwise()
        assertEquals(setOf(Direction.UP, Direction.RIGHT), t0.connections)
        assertEquals(setOf(Direction.RIGHT, Direction.DOWN), t90.connections)
        assertEquals(setOf(Direction.DOWN, Direction.LEFT), t180.connections)
        assertEquals(setOf(Direction.LEFT, Direction.UP), t270.connections)
        assertEquals(0, t270.rotatedClockwise().rotation)
    }

    @Test fun oppositeDirections() {
        assertEquals(Direction.DOWN, opposite(Direction.UP))
        assertEquals(Direction.LEFT, opposite(Direction.RIGHT))
    }

    @Test fun neighborReturnsNullOutsideBoard() {
        assertNull(neighbor(pos(0, 0), Direction.UP))
        assertNull(neighbor(pos(5, 5), Direction.RIGHT))
        assertEquals(pos(2, 1), neighbor(pos(2, 0), Direction.RIGHT))
    }
}

class ConnectionTest {
    @Test fun horizontalTilesConnect() {
        val board = boardWith(pos(0, 0) to tile(S), pos(0, 1) to tile(S))
        assertEquals(pos(0, 1), PathFinder.connectedNeighbor(board, pos(0, 0), Direction.RIGHT))
    }

    @Test fun verticalTilesConnect() {
        val board = boardWith(pos(0, 0) to tile(S, 90), pos(1, 0) to tile(S, 90))
        assertEquals(pos(1, 0), PathFinder.connectedNeighbor(board, pos(0, 0), Direction.DOWN))
    }

    @Test fun cornerConnectsToMatchingNeighbors() {
        // corner at (1,1) opening up + right, vertical above, horizontal to the right
        val board = boardWith(pos(1, 1) to tile(C), pos(0, 1) to tile(S, 90), pos(1, 2) to tile(S))
        assertEquals(pos(0, 1), PathFinder.connectedNeighbor(board, pos(1, 1), Direction.UP))
        assertEquals(pos(1, 2), PathFinder.connectedNeighbor(board, pos(1, 1), Direction.RIGHT))
    }

    @Test fun mismatchedEdgesDoNotConnect() {
        val board = boardWith(pos(0, 0) to tile(S), pos(0, 1) to tile(S, 90))
        assertNull(PathFinder.connectedNeighbor(board, pos(0, 0), Direction.RIGHT))
    }

    @Test fun startConnectsOnlyThroughItsRightSide() {
        val matching = boardWith(pos(2, 1) to tile(S))
        assertEquals(pos(2, 1), PathFinder.connectedNeighbor(matching, pos(2, 0), Direction.RIGHT))
        val vertical = boardWith(pos(1, 0) to tile(S, 90), pos(3, 0) to tile(S, 90))
        assertEquals(setOf(pos(2, 0)), PathFinder.connectedFromStart(vertical))
    }

    @Test fun finishConnectsOnlyThroughItsLeftSide() {
        val matching = boardWith(pos(3, 4) to tile(S))
        assertEquals(pos(3, 5), PathFinder.connectedNeighbor(matching, pos(3, 4), Direction.RIGHT))
        val fromAbove = boardWith(pos(2, 5) to tile(S, 90))
        assertNull(PathFinder.connectedNeighbor(fromAbove, pos(2, 5), Direction.DOWN))
    }
}

class PathFindingTest {
    @Test fun completeRouteIsDetected() {
        val path = PathFinder.findWinningPath(boardWith(*solution.toTypedArray()))
        assertNotNull(path)
        assertEquals(pos(2, 0), path!!.first())
        assertEquals(pos(3, 5), path.last())
        assertEquals(9, path.size)
    }

    @Test fun disconnectedRouteIsNotWinning() {
        val broken = solution.filterIndexed { i, _ -> i != 3 }
        assertNull(PathFinder.findWinningPath(boardWith(*broken.toTypedArray())))
    }

    @Test fun wrongRotationBreaksRoute() {
        val wrong = solution.toMutableList().apply { this[4] = pos(4, 3) to tile(C, 0) }
        assertNull(PathFinder.findWinningPath(boardWith(*wrong.toTypedArray())))
    }
}

class GameRulesTest {
    @Test fun placementWithoutSelectionIsRejected() {
        val game = PathBuilderGame()
        assertEquals(PlacementResult.Rejected(RejectReason.NO_TILE_SELECTED), game.place(pos(0, 0)))
    }

    @Test fun blockedCellIsRejected() {
        val game = PathBuilderGame()
        game.select(game.state.tray.first().id)
        assertEquals(PlacementResult.Rejected(RejectReason.BLOCKED), game.place(pos(2, 2)))
        assertEquals(0, game.state.moves)
    }

    @Test fun startAndFinishAreRejected() {
        val game = PathBuilderGame()
        game.select(game.state.tray.first().id)
        assertEquals(PlacementResult.Rejected(RejectReason.START_OR_FINISH), game.place(pos(2, 0)))
        assertEquals(PlacementResult.Rejected(RejectReason.START_OR_FINISH), game.place(pos(3, 5)))
    }

    @Test fun occupiedCellIsRejected() {
        val game = PathBuilderGame()
        game.select(game.state.tray[0].id)
        game.place(pos(0, 0))
        game.select(game.state.tray[0].id)
        assertEquals(PlacementResult.Rejected(RejectReason.OCCUPIED), game.place(pos(0, 0)))
        assertEquals(1, game.state.moves)
    }

    @Test fun placementRefillsTrayFromBag() {
        val game = PathBuilderGame()
        val before = game.state
        game.select(before.tray[1].id)
        game.place(pos(0, 0))
        assertEquals(3, game.state.tray.size)
        assertEquals(before.bag.first(), game.state.tray[1])
        assertEquals(before.bag.size - 1, game.state.bag.size)
        assertNull(game.state.selectedTileId)
    }

    @Test fun solvingWithTheRealBagCompletes() {
        val game = PathBuilderGame()
        val results = solution.map { (p, t) -> game.play(p, t.kind, t.rotation) }
        assertEquals(PlacementResult.Connected, results.first())
        assertEquals(PlacementResult.Completed, results.last())
        assertTrue(game.state.isComplete)
        assertEquals(7, game.state.moves)
        assertEquals(7, game.state.pathLength)
        assertEquals(5, game.state.tilesRemaining)
    }

    @Test fun noInputAfterCompletion() {
        val game = PathBuilderGame()
        solution.forEach { (p, t) -> game.play(p, t.kind, t.rotation) }
        assertFalse(game.select(game.state.tray.first().id))
        assertFalse(game.rotateSelected())
        assertFalse(game.undo())
        assertEquals(PlacementResult.Rejected(RejectReason.GAME_OVER), game.place(pos(0, 0)))
    }

    @Test fun exhaustingTilesWithoutRouteFails() {
        val game = PathBuilderGame()
        assertEquals(PlacementResult.Failed, game.wasteAllTiles())
        assertTrue(game.state.isFailed)
        assertFalse(game.state.isComplete)
        assertEquals(12, game.state.moves)
    }

    @Test fun placementIgnoredWhileAnimating() {
        val game = PathBuilderGame()
        game.select(game.state.tray.first().id)
        game.setAnimating(true)
        assertEquals(PlacementResult.Rejected(RejectReason.BUSY), game.place(pos(0, 0)))
        assertFalse(game.undo())
        assertTrue(game.rotateSelected())
    }

    @Test fun undoRestoresPreviousState() {
        val game = PathBuilderGame()
        val before = game.state
        val t = before.tray.first()
        game.select(t.id)
        game.rotateSelected()
        val selectedBefore = game.state
        game.place(pos(0, 0))
        game.updateElapsed(5_000)

        assertTrue(game.undo())
        assertEquals(PathCell.Empty, game.state.board.cellAt(pos(0, 0)))
        assertEquals(selectedBefore.tray, game.state.tray)
        assertEquals(before.bag, game.state.bag)
        assertEquals(0, game.state.moves)
        assertEquals(t.id, game.state.selectedTileId)
        assertEquals(5_000, game.state.elapsedMillis)
        assertFalse(game.undo())
    }

    @Test fun restartRestoresInitialPuzzle() {
        val game = PathBuilderGame()
        val initial = game.state
        game.wasteAllTiles()
        game.restart()
        assertEquals(initial, game.state)
        assertFalse(game.canUndo)
    }
}
