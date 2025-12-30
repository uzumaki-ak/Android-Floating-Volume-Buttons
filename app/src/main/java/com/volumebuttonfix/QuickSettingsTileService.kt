package com.volumebuttonfix

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

/**
 * QuickSettingsTileService - Quick Settings tile to toggle service
 *
 * Adds a tile to Quick Settings panel (swipe down from top)
 * User can tap to start/stop floating button service
 *
 * Path: app/src/main/java/com/volumebuttonfix/QuickSettingsTileService.kt
 * Action: CREATE NEW FILE
 */
@RequiresApi(Build.VERSION_CODES.N)
class QuickSettingsTileService : TileService() {

    /**
     * Called when tile is added or visible
     */
    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    /**
     * Called when user taps the tile
     */
    override fun onClick() {
        super.onClick()

        if (isServiceRunning()) {
            // Stop service
            val intent = Intent(this, OverlayService::class.java)
            stopService(intent)
        } else {
            // Start service
            val intent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        // Update tile state after a short delay
        qsTile?.let { tile ->
            android.os.Handler(mainLooper).postDelayed({
                updateTileState()
            }, 500)
        }
    }

    /**
     * Update tile visual state based on service status
     */
    private fun updateTileState() {
        qsTile?.let { tile ->
            val isRunning = isServiceRunning()

            tile.state = if (isRunning) {
                Tile.STATE_ACTIVE
            } else {
                Tile.STATE_INACTIVE
            }

            tile.label = if (isRunning) {
                "Floating Buttons ON"
            } else {
                "Floating Buttons OFF"
            }

            tile.updateTile()
        }
    }

    /**
     * Check if overlay service is running
     */
    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (OverlayService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}