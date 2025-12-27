package com.volumebuttonfix

import android.content.Context
import android.media.AudioManager
import android.widget.Toast

/**
 * VolumeController - Handles all volume-related operations
 *
 * This class manages the device's audio volume:
 * - Increase volume
 * - Decrease volume
 * - Mute/Unmute
 *
 * For beginners: This is a helper class that talks to Android's audio system.
 * It wraps the complex AudioManager API into simple functions.
 */
class VolumeController(private val context: Context) {

    // AudioManager - System service that controls device audio
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // The stream type we'll control (STREAM_MUSIC = media/music volume)
    // Other options include: STREAM_RING (ringtone), STREAM_ALARM, etc.
    private val streamType = AudioManager.STREAM_MUSIC

    // Remember mute state
    private var isMuted = false
    private var volumeBeforeMute = 0

    /**
     * Increase the volume by one step
     * This is equivalent to pressing the physical volume up button
     */
    fun increaseVolume() {
        try {
            // If currently muted, unmute first
            if (isMuted) {
                toggleMute()
            }

            // Increase volume by 1 step
            // FLAG_SHOW_UI shows the volume slider on screen
            // FLAG_PLAY_SOUND plays the volume change sound
            audioManager.adjustStreamVolume(
                streamType,
                AudioManager.ADJUST_RAISE,  // Increase volume
                AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
            )

        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to increase volume")
        }
    }

    /**
     * Decrease the volume by one step
     * This is equivalent to pressing the physical volume down button
     */
    fun decreaseVolume() {
        try {
            // If currently muted, unmute first
            if (isMuted) {
                toggleMute()
            }

            // Decrease volume by 1 step
            audioManager.adjustStreamVolume(
                streamType,
                AudioManager.ADJUST_LOWER,  // Decrease volume
                AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
            )

        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to decrease volume")
        }
    }

    /**
     * Toggle mute/unmute
     * Mute: Save current volume and set to 0
     * Unmute: Restore previous volume
     */
    fun toggleMute() {
        try {
            if (isMuted) {
                // Currently muted - unmute
                unmute()
            } else {
                // Currently not muted - mute
                mute()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to toggle mute")
        }
    }

    /**
     * Mute the audio
     * Saves current volume and sets volume to 0
     */
    private fun mute() {
        // Get current volume before muting
        volumeBeforeMute = audioManager.getStreamVolume(streamType)

        // Set volume to 0
        audioManager.setStreamVolume(
            streamType,
            0,  // Volume level 0 (muted)
            AudioManager.FLAG_SHOW_UI
        )

        isMuted = true

        Toast.makeText(
            context,
            "Muted",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Unmute the audio
     * Restores the volume to what it was before muting
     */
    private fun unmute() {
        // Restore previous volume
        // If previous volume was 0, set it to at least 1
        val volumeToRestore = if (volumeBeforeMute == 0) 1 else volumeBeforeMute

        audioManager.setStreamVolume(
            streamType,
            volumeToRestore,
            AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
        )

        isMuted = false

        Toast.makeText(
            context,
            "Unmuted",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Get the current volume level
     * Useful for displaying volume status
     *
     * @return Current volume level (0 to max)
     */
    fun getCurrentVolume(): Int {
        return audioManager.getStreamVolume(streamType)
    }

    /**
     * Get the maximum possible volume level
     *
     * @return Maximum volume level
     */
    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(streamType)
    }

    /**
     * Check if audio is currently muted
     *
     * @return true if muted, false otherwise
     */
    fun isMuted(): Boolean {
        return isMuted || getCurrentVolume() == 0
    }

    /**
     * Show an error toast message
     *
     * @param message Error message to display
     */
    private fun showError(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}