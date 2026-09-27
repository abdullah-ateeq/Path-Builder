package com.example.pathbuilder.ui.screens

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pathbuilder.data.SettingsRepository
import com.example.pathbuilder.feedback.FeedbackEvent
import com.example.pathbuilder.game.GridPosition
import com.example.pathbuilder.game.PathBuilderGame
import com.example.pathbuilder.game.PathBuilderState
import com.example.pathbuilder.game.PlacementResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameResult(
    val success: Boolean,
    val moves: Int,
    val timeMillis: Long,
    val tilesRemaining: Int,
    val pathLength: Int,
)

class GameViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val game = PathBuilderGame()

    private val _state = MutableStateFlow(game.state)
    val state: StateFlow<PathBuilderState> = _state.asStateFlow()

    /** Kept separate from [state] so the ticking clock only recomposes the HUD. */
    private val _elapsed = MutableStateFlow(0L)
    val elapsed: StateFlow<Long> = _elapsed.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<FeedbackEvent> = _events

    private var accumulatedMillis = 0L
    private var resumedAt: Long? = null
    private var tickJob: Job? = null
    private var animationJob: Job? = null
    private var recorded = false

    fun selectTile(id: Int) {
        if (game.select(id)) {
            publish()
            emit(FeedbackEvent.SELECT)
        }
    }

    fun rotateSelected() {
        if (game.rotateSelected()) {
            publish()
            emit(FeedbackEvent.ROTATE)
        }
    }

    fun placeAt(position: GridPosition) {
        when (val result = game.place(position)) {
            is PlacementResult.Rejected -> {
                if (game.state.selectedTile != null && !game.state.isOver) emit(FeedbackEvent.REJECT)
                return
            }
            PlacementResult.Placed -> emit(FeedbackEvent.PLACE)
            PlacementResult.Connected -> emit(FeedbackEvent.CONNECT)
            PlacementResult.Completed -> emit(FeedbackEvent.COMPLETE)
            PlacementResult.Failed -> emit(FeedbackEvent.FAIL)
        }
        if (game.state.isOver) finish() else lockDuringPlacementAnimation()
        publish()
    }

    fun undo() {
        if (game.undo()) {
            animationJob?.cancel()
            publish()
            emit(FeedbackEvent.UNDO)
        }
    }

    fun restart() {
        animationJob?.cancel()
        game.restart()
        recorded = false
        accumulatedMillis = 0L
        _elapsed.value = 0L
        if (resumedAt != null) resumedAt = SystemClock.elapsedRealtime()
        publish()
    }

    fun resumeTimer() {
        if (game.state.isOver || tickJob != null) return
        resumedAt = SystemClock.elapsedRealtime()
        tickJob = viewModelScope.launch {
            while (isActive) {
                _elapsed.value = currentElapsed()
                delay(250)
            }
        }
    }

    fun pauseTimer() {
        accumulatedMillis = currentElapsed()
        resumedAt = null
        tickJob?.cancel()
        tickJob = null
        _elapsed.value = accumulatedMillis
    }

    fun result(): GameResult {
        val s = game.state
        return GameResult(s.isComplete, s.moves, s.elapsedMillis, s.tilesRemaining, s.pathLength)
    }

    private fun currentElapsed(): Long =
        accumulatedMillis + (resumedAt?.let { SystemClock.elapsedRealtime() - it } ?: 0L)

    private fun lockDuringPlacementAnimation() {
        game.setAnimating(true)
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            delay(PLACEMENT_ANIMATION_MS)
            game.setAnimating(false)
            publish()
        }
    }

    private fun finish() {
        pauseTimer()
        game.updateElapsed(accumulatedMillis)
        if (recorded) return
        recorded = true
        val s = game.state
        viewModelScope.launch { repository.recordGame(s.isComplete, s.moves, s.elapsedMillis) }
    }

    private fun publish() {
        _state.value = game.state
        _canUndo.value = game.canUndo || (game.state.isAnimating && !game.state.isOver && game.state.moves > 0)
    }

    private fun emit(event: FeedbackEvent) {
        _events.tryEmit(event)
    }

    companion object {
        const val PLACEMENT_ANIMATION_MS = 160L

        fun factory(repository: SettingsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameViewModel(repository) }
        }
    }
}
