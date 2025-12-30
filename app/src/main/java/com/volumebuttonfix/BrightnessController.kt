package com.volumebuttonfix

import android.content.Context
import android.provider.Settings
import android.widget.Toast

/**
 * BrightnessController - Controls screen brightness
 *
 * Handles increasing/decreasing screen brightness level.
 * Brightness ranges from 0-255 (0 = minimum, 255 = maximum)
 *
 * Path: app/src/main/java/com/volumebuttonfix/BrightnessController.kt
 * Action: CREATE NEW FILE
 */
class BrightnessController(private val context: Context) {

    companion object {
        private const val MIN_BRIGHTNESS = 0
        private const val MAX_BRIGHTNESS = 255
        private const val BRIGHTNESS_STEP = 25 // Change by ~10% each press
    }

    /**
     * Check if app has permission to modify system brightness
     */
    fun hasPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    /**
     * Get current brightness level (0-255)
     */
    private fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Settings.SettingNotFoundException) {
            128 // Default to 50%
        }
    }

    /**
     * Set brightness level (0-255)
     */
    private fun setBrightness(brightness: Int) {
        try {
            val clampedBrightness = brightness.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                clampedBrightness
            )

            // Show percentage
            val percentage = (clampedBrightness * 100) / MAX_BRIGHTNESS
            Toast.makeText(
                context,
                "Brightness: $percentage%",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to change brightness",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Increase brightness by one step
     */
    fun increaseBrightness() {
        if (!hasPermission()) {
            showPermissionError()
            return
        }

        val current = getCurrentBrightness()
        val newBrightness = (current + BRIGHTNESS_STEP).coerceAtMost(MAX_BRIGHTNESS)
        setBrightness(newBrightness)
    }

    /**
     * Decrease brightness by one step
     */
    fun decreaseBrightness() {
        if (!hasPermission()) {
            showPermissionError()
            return
        }

        val current = getCurrentBrightness()
        val newBrightness = (current - BRIGHTNESS_STEP).coerceAtLeast(MIN_BRIGHTNESS)
        setBrightness(newBrightness)
    }

    /**
     * Show error message when permission is missing
     */
    private fun showPermissionError() {
        Toast.makeText(
            context,
            "Brightness permission required. Enable in Settings.",
            Toast.LENGTH_LONG
        ).show()
    }
}