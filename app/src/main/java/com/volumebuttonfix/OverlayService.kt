package com.volumebuttonfix

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.WindowManager

/**
 * FIXED OverlayService - Now supports real-time settings updates
 *
 * Path: app/src/main/java/com/volumebuttonfix/OverlayService.kt
 * Action: REPLACE ENTIRE FILE
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingButtonView: FloatingButtonView? = null
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var volumeController: VolumeController
    private lateinit var screenLockHelper: ScreenLockHelper

    // Broadcast receiver for settings changes
    private val settingsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SETTINGS_CHANGED) {
                // Refresh floating view with new settings
                floatingButtonView?.refreshSettings()
            }
        }
    }

    companion object {
        const val ACTION_SETTINGS_CHANGED = "com.volumebuttonfix.SETTINGS_CHANGED"
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        notificationHelper = NotificationHelper(this)
        volumeController = VolumeController(this)
        screenLockHelper = ScreenLockHelper(this)

        // Register broadcast receiver for settings updates
        val filter = IntentFilter(ACTION_SETTINGS_CHANGED)
        registerReceiver(settingsUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationHelper.createServiceNotification()
        startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)

        if (floatingButtonView == null) {
            createFloatingButtons()
        }

        return START_STICKY
    }

    private fun createFloatingButtons() {
        try {
            floatingButtonView = FloatingButtonView(
                context = this,
                windowManager = windowManager,
                volumeController = volumeController,
                screenLockHelper = screenLockHelper,
                onRemove = {
                    stopSelf()
                }
            )

            floatingButtonView?.addToWindow()

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister receiver
        try {
            unregisterReceiver(settingsUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        floatingButtonView?.removeFromWindow()
        floatingButtonView = null

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}