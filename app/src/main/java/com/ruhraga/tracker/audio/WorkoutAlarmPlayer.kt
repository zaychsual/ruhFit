package com.ruhraga.tracker.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.ruhraga.tracker.R

/**
 * Plays the workout rest-complete alarm (R.raw.electric_eel) with configurable volume.
 */
object WorkoutAlarmPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun play(
        context: Context,
        volume: Float,
        onCompleted: () -> Unit = {}
    ) {
        stop()
        val clampedVolume = volume.coerceIn(0f, 1f)
        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            val afd = context.resources.openRawResourceFd(R.raw.electric_eel)
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            setVolume(clampedVolume, clampedVolume)
            setOnCompletionListener {
                stop()
                onCompleted()
            }
            prepare()
            start()
        }
        mediaPlayer = player
    }

    fun updateVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun stop() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }
}
