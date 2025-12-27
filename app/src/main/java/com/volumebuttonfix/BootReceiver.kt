package com.volumebuttonfix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * BootReceiver - Auto-starts service after device reboot
 *
 * This receiver listens for the BOOT_COMPLETED broadcast.
 * When the device finishes booting, this receiver automatically
 * starts the floating button service if it was running before.
 *
 * For beginners: BroadcastReceiver is like a listener that waits
 * for system-wide events (like device boot, network changes, etc.)
 * and responds to them automatically.
 */
class BootReceiver : BroadcastReceiver() {

    /**
     * onReceive - Called when the broadcast is received
     *
     * @param context - Application context
     * @param intent - The Intent containing information about the broadcast
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Check if this is the BOOT_COMPLETED broadcast
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Optional: Check if user wants auto-start
            // For now, we'll always auto-start
            // In future, you could add a setting to enable/disable this

            try {
                // Create intent to start the overlay service
                val serviceIntent = Intent(context, OverlayService::class.java)

                // Start the service based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ requires startForegroundService
                    context.startForegroundService(serviceIntent)
                } else {
                    // Older versions use regular startService
                    context.startService(serviceIntent)
                }

            } catch (e: Exception) {
                // If service fails to start, just log the error
                // Don't crash the boot process
                e.printStackTrace()
            }
        }
    }
}