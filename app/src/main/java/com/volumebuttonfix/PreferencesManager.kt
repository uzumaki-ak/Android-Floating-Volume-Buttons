package com.volumebuttonfix

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "VolumeButtonFixPrefs",
        Context.MODE_PRIVATE
    )

    // Button Enable/Disable States
    fun isButtonEnabled(buttonType: ButtonType): Boolean {
        return prefs.getBoolean("button_${buttonType.name}", buttonType.defaultEnabled)
    }

    fun setButtonEnabled(buttonType: ButtonType, enabled: Boolean) {
        prefs.edit().putBoolean("button_${buttonType.name}", enabled).apply()
    }

    // Size Setting (0 = Small, 1 = Medium, 2 = Large)
    fun getButtonSize(): Int {
        return prefs.getInt("button_size", 1)
    }

    fun setButtonSize(size: Int) {
        prefs.edit().putInt("button_size", size).apply()
    }

    fun getButtonSizeDp(): Int {
        return when (getButtonSize()) {
            0 -> 48
            1 -> 56
            2 -> 64
            else -> 56
        }
    }

    // Transparency (50-100)
    fun getTransparency(): Int {
        return prefs.getInt("transparency", 80)
    }

    fun setTransparency(transparency: Int) {
        prefs.edit().putInt("transparency", transparency).apply()
    }

    fun getTransparencyAlpha(): Float {
        return getTransparency() / 100f
    }

    // Color Theme (0-4)
    fun getColorTheme(): Int {
        return prefs.getInt("color_theme", 0)
    }

    fun setColorTheme(theme: Int) {
        prefs.edit().putInt("color_theme", theme).apply()
    }

    fun getColorThemeValue(context: Context): Int {
        return when (getColorTheme()) {
            0 -> context.getColor(R.color.primary)
            1 -> context.getColor(R.color.red_500)
            2 -> context.getColor(R.color.green_500)
            3 -> 0xFF9C27B0.toInt()
            4 -> 0xFF424242.toInt()
            else -> context.getColor(R.color.primary)
        }
    }

    // Auto-Hide Settings
    fun isAutoHideEnabled(): Boolean {
        return prefs.getBoolean("auto_hide_enabled", false)
    }

    fun setAutoHideEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_hide_enabled", enabled).apply()
    }

    fun getAutoHideDelay(): Long {
        return prefs.getLong("auto_hide_delay", 10000)
    }

    fun setAutoHideDelay(delayMs: Long) {
        prefs.edit().putLong("auto_hide_delay", delayMs).apply()
    }

    // Gestures Enabled
    fun isGesturesEnabled(): Boolean {
        return prefs.getBoolean("gestures_enabled", true)
    }

    fun setGesturesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("gestures_enabled", enabled).apply()
    }

    // Collapsed State
    fun isCollapsed(): Boolean {
        return prefs.getBoolean("is_collapsed", true)
    }

    fun setCollapsed(collapsed: Boolean) {
        prefs.edit().putBoolean("is_collapsed", collapsed).apply()
    }

    // Position
    fun getPositionX(): Int {
        return prefs.getInt("position_x", -1)
    }

    fun getPositionY(): Int {
        return prefs.getInt("position_y", -1)
    }

    fun setPosition(x: Int, y: Int) {
        prefs.edit()
            .putInt("position_x", x)
            .putInt("position_y", y)
            .apply()
    }

    // ✅ AUTO-POSITION (ADD THIS)
    fun isAutoPositionEnabled(): Boolean {
        return prefs.getBoolean("auto_position", true)
    }

    fun setAutoPositionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_position", enabled).apply()
    }

    // Usage Statistics
    fun incrementButtonPress(buttonType: ButtonType) {
        val key = "stats_${buttonType.name}"
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getButtonPressCount(buttonType: ButtonType): Int {
        return prefs.getInt("stats_${buttonType.name}", 0)
    }

    fun getTotalButtonPresses(): Int {
        var total = 0
        ButtonType.values().forEach { type ->
            total += getButtonPressCount(type)
        }
        return total
    }

    fun resetStatistics() {
        val editor = prefs.edit()
        ButtonType.values().forEach { type ->
            editor.remove("stats_${type.name}")
        }
        editor.apply()
    }

    enum class ButtonType(val defaultEnabled: Boolean) {
        VOLUME_UP(true),
        VOLUME_DOWN(true),
        MUTE(true),
        LOCK(true),
        BRIGHTNESS_UP(false),
        BRIGHTNESS_DOWN(false),
        HOME(false),
        RECENT_APPS(false),
        FLASHLIGHT(false),
        WIFI(false),
        BLUETOOTH(false)
    }
}
