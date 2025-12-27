package com.volumebuttonfix

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager

/**
 * OverlayService - Background service that displays floating buttons
 *
 * This service runs in the background and manages the floating button overlay.
 * It runs as a FOREGROUND SERVICE, which means:
 * - It shows a persistent notification (required by Android)
 * - Android won't kill it easily
 * - It can run even when app is closed
 *
 * For beginners: A Service is like an invisible component that runs in the background
 * even when your app is not visible on screen.
 */
class OverlayService : Service() {

    // WindowManager - System service that manages windows and overlays
    private lateinit var windowManager: WindowManager

    // Our custom floating button view
    private var floatingButtonView: FloatingButtonView? = null

    // Helper for notifications
    private lateinit var notificationHelper: NotificationHelper

    // Volume controller
    private lateinit var volumeController: VolumeController

    // Screen lock helper
    private lateinit var screenLockHelper: ScreenLockHelper

    /**
     * onCreate - Called when service is first created
     * Initialize all components here
     */
    override fun onCreate() {
        super.onCreate()

        // Get the WindowManager system service
        // This lets us add views to the screen overlay
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Initialize helpers
        notificationHelper = NotificationHelper(this)
        volumeController = VolumeController(this)
        screenLockHelper = ScreenLockHelper(this)
    }

    /**
     * onStartCommand - Called each time the service is started
     * This is where we set up the floating buttons and notification
     *
     * @param intent - The Intent that started this service
     * @param flags - Additional data about the start request
     * @param startId - Unique ID for this start request
     * @return START_STICKY - Service should be restarted if killed by system
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create and show the persistent notification
        // This is required for foreground services on Android 8.0+
        val notification = notificationHelper.createServiceNotification()
        startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)

        // Create and display the floating buttons if not already shown
        if (floatingButtonView == null) {
            createFloatingButtons()
        }

        // START_STICKY means if Android kills this service due to low memory,
        // it will try to restart it when resources are available
        return START_STICKY
    }

    /**
     * Create the floating button overlay and add it to the screen
     */
    private fun createFloatingButtons() {
        try {
            // Create the floating button view
            floatingButtonView = FloatingButtonView(
                context = this,
                windowManager = windowManager,
                volumeController = volumeController,
                screenLockHelper = screenLockHelper,
                onRemove = {
                    // Called when user wants to remove floating buttons
                    stopSelf()
                }
            )

            // Add the view to the screen
            floatingButtonView?.addToWindow()

        } catch (e: Exception) {
            // If something goes wrong, log it and stop the service
            e.printStackTrace()
            stopSelf()
        }
    }

    /**
     * onDestroy - Called when service is being stopped
     * Clean up resources here
     */
    override fun onDestroy() {
        super.onDestroy()

        // Remove the floating buttons from screen
        floatingButtonView?.removeFromWindow()
        floatingButtonView = null

        // Remove the notification
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    /**
     * onBind - Required method for Service class
     * We don't use bound services, so return null
     *
     * For beginners: Services can be "bound" (connected to activities) or "started"
     * (run independently). We use started service, so we don't need binding.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}