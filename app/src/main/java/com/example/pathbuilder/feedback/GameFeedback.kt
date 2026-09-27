package com.example.pathbuilder.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import com.example.pathbuilder.data.GameSettings

enum class FeedbackEvent { SELECT, ROTATE, PLACE, CONNECT, COMPLETE, FAIL, UNDO, REJECT }

/** Routes game events to sound and haptics, honouring the player's settings. */
class GameFeedback(private val sound: SoundPlayer, private val view: View) {

    var settings: GameSettings = GameSettings()

    fun perform(event: FeedbackEvent) {
        if (settings.soundEnabled) soundFor(event)?.let(sound::play)
        if (settings.hapticsEnabled) view.performHapticFeedback(hapticFor(event))
    }

    fun release() = sound.release()

    private fun soundFor(event: FeedbackEvent): Sfx? = when (event) {
        FeedbackEvent.SELECT -> Sfx.SELECT
        FeedbackEvent.ROTATE, FeedbackEvent.UNDO -> Sfx.ROTATE
        FeedbackEvent.PLACE -> Sfx.PLACE
        FeedbackEvent.CONNECT -> Sfx.CONNECT
        FeedbackEvent.COMPLETE -> Sfx.COMPLETE
        FeedbackEvent.FAIL -> Sfx.FAIL
        FeedbackEvent.REJECT -> null
    }

    private fun hapticFor(event: FeedbackEvent): Int = when (event) {
        FeedbackEvent.SELECT, FeedbackEvent.ROTATE, FeedbackEvent.UNDO -> HapticFeedbackConstants.CLOCK_TICK
        FeedbackEvent.PLACE, FeedbackEvent.CONNECT -> HapticFeedbackConstants.VIRTUAL_KEY
        FeedbackEvent.COMPLETE ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.LONG_PRESS
        FeedbackEvent.FAIL, FeedbackEvent.REJECT ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.LONG_PRESS
    }
}

val LocalGameFeedback = staticCompositionLocalOf<GameFeedback?> { null }

@Composable
fun rememberGameFeedback(settings: GameSettings): GameFeedback {
    val view = LocalView.current
    val feedback = remember(view) { GameFeedback(SoundPlayer(), view) }
    feedback.settings = settings
    DisposableEffect(feedback) { onDispose { feedback.release() } }
    return feedback
}
