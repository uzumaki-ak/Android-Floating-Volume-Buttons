package com.volumebuttonfix

import android.content.Context

/**
 * UsageStatsManager - Tracks button usage statistics
 *
 * Path: app/src/main/java/com/volumebuttonfix/UsageStatsManager.kt
 * Action: CREATE NEW FILE
 */
class UsageStatsManager(private val context: Context) {

    private val prefsManager = PreferencesManager(context)

    /**
     * Record a button press
     */
    fun recordButtonPress(buttonType: PreferencesManager.ButtonType) {
        prefsManager.incrementButtonPress(buttonType)
    }

    /**
     * Get total presses for a specific button
     */
    fun getButtonPresses(buttonType: PreferencesManager.ButtonType): Int {
        return prefsManager.getButtonPressCount(buttonType)
    }

    /**
     * Get total presses across all buttons
     */
    fun getTotalPresses(): Int {
        return prefsManager.getTotalButtonPresses()
    }

    /**
     * Get most used button
     */
    fun getMostUsedButton(): Pair<PreferencesManager.ButtonType, Int> {
        var maxButton = PreferencesManager.ButtonType.VOLUME_UP
        var maxCount = 0

        PreferencesManager.ButtonType.values().forEach { type ->
            val count = getButtonPresses(type)
            if (count > maxCount) {
                maxCount = count
                maxButton = type
            }
        }

        return Pair(maxButton, maxCount)
    }

    /**
     * Get statistics summary
     */
    fun getStatsSummary(): String {
        val total = getTotalPresses()
        val (mostUsed, mostUsedCount) = getMostUsedButton()

        return buildString {
            append("Total button presses: $total\n")
            append("Most used: ${mostUsed.name.replace("_", " ")} ($mostUsedCount times)")
        }
    }

    /**
     * Reset all statistics
     */
    fun resetStats() {
        prefsManager.resetStatistics()
    }
}