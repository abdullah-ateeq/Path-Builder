package com.example.pathbuilder.feedback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

enum class Sfx { SELECT, ROTATE, PLACE, CONNECT, COMPLETE, FAIL }

/**
 * Tiny synthesized sound set: soft sine tones with a fast attack and exponential decay,
 * rendered once into static AudioTracks. No audio assets needed.
 */
class SoundPlayer {

    private val tracks: Map<Sfx, AudioTrack> = buildMap {
        Sfx.entries.forEach { sfx -> createTrack(render(sfx))?.let { put(sfx, it) } }
    }

    fun play(sfx: Sfx) {
        val track = tracks[sfx] ?: return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    fun release() {
        tracks.values.forEach { runCatching { it.release() } }
    }

    private fun render(sfx: Sfx): ShortArray = when (sfx) {
        Sfx.SELECT -> mix(Note(1320.0, 0, 45, 0.16, 90.0))
        Sfx.ROTATE -> mix(Note(990.0, 0, 50, 0.12, 70.0), Note(1980.0, 0, 30, 0.04, 120.0))
        Sfx.PLACE -> mix(Note(196.0, 0, 110, 0.30, 38.0), Note(392.0, 0, 70, 0.14, 55.0), Note(2400.0, 0, 12, 0.05, 300.0))
        Sfx.CONNECT -> mix(Note(659.3, 0, 120, 0.14, 28.0), Note(987.8, 60, 160, 0.14, 22.0))
        Sfx.COMPLETE -> mix(
            Note(523.3, 0, 420, 0.13, 7.0),
            Note(659.3, 110, 420, 0.13, 7.0),
            Note(784.0, 220, 460, 0.13, 6.5),
            Note(1046.5, 330, 620, 0.12, 5.0),
        )
        Sfx.FAIL -> mix(Note(392.0, 0, 260, 0.15, 10.0), Note(293.7, 180, 380, 0.15, 8.0))
    }

    private data class Note(val freq: Double, val startMs: Int, val durationMs: Int, val volume: Double, val decay: Double)

    private fun mix(vararg notes: Note): ShortArray {
        val totalMs = notes.maxOf { it.startMs + it.durationMs }
        val out = DoubleArray(SAMPLE_RATE * totalMs / 1000)
        for (n in notes) {
            val offset = SAMPLE_RATE * n.startMs / 1000
            val length = SAMPLE_RATE * n.durationMs / 1000
            val attack = SAMPLE_RATE * 4 / 1000
            for (i in 0 until min(length, out.size - offset)) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = min(1.0, i.toDouble() / attack) * exp(-t * n.decay) * min(1.0, (length - i).toDouble() / attack)
                out[offset + i] += sin(2 * PI * n.freq * t) * envelope * n.volume
            }
        }
        return ShortArray(out.size) { (out[it].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort() }
    }

    private fun createTrack(samples: ShortArray): AudioTrack? = runCatching {
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * 2)
            .build()
            .also { it.write(samples, 0, samples.size) }
    }.getOrNull()

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
