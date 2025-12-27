package com.volumebuttonfix

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * NotificationHelper - Manages app notifications
 *
 * This class handles creating and managing notifications.
 * A persistent notification is required for foreground services on Android 8.0+
 *
 * For beginners: A notification is the icon/message that appears in the
 * notification bar at the top of the screen. Foreground services MUST
 * show a notification so users know the app is running in background.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        // Notification channel ID - unique identifier for our notification channel
        private const val CHANNEL_ID = "volume_button_fix_service"

        // Notification channel name - shown to user in notification settings
        private const val CHANNEL_NAME = "Floating Buttons Service"

        // Notification channel description
        private const val CHANNEL_DESCRIPTION = "Shows when floating buttons are active"

        // Notification ID for our service notification
        const val SERVICE_NOTIFICATION_ID = 1001
    }

    // NotificationManager - System service for managing notifications
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Create notification channel on Android 8.0+
        // Channels are required for notifications on newer Android versions
        createNotificationChannel()
    }

    /**
     * Create notification channel (required for Android 8.0+)
     *
     * For beginners: Notification Channels let users control notification
     * settings for different types of notifications from your app.
     * For example, they can silence "service" notifications but allow "alert" notifications.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW  // Low importance = no sound, just shows in status bar
            ).apply {
                description = CHANNEL_DESCRIPTION
                // Don't play sound for this notification
                setSound(null, null)
                // Don't show badge on app icon
                setShowBadge(false)
            }

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create the persistent service notification
     * This notification is shown while the floating buttons service is running
     *
     * @return Notification object ready to be displayed
     */
    fun createServiceNotification(): Notification {
        // Create intent to open MainActivity when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create pending intent (intent that will be triggered in the future)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Build the notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            // Small icon (shown in status bar)
            .setSmallIcon(R.drawable.ic_volume_up)

            // Notification title
            .setContentTitle("Floating Buttons Active")

            // Notification text
            .setContentText("Tap to open app")

            // What happens when user taps the notification
            .setContentIntent(pendingIntent)

            // Priority (for Android 7.1 and lower)
            .setPriority(NotificationCompat.PRIORITY_LOW)

            // Category
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

            // Make notification ongoing (can't be swiped away)
            .setOngoing(true)

            // Don't show timestamp
            .setShowWhen(false)

            // Build and return the notification
            .build()
    }

    /**
     * Update the service notification
     * Call this if you want to change notification text while service is running
     *
     * @param notification Updated notification to display
     */
    fun updateNotification(notification: Notification) {
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification)
    }

    /**
     * Cancel the service notification
     * Removes the notification from the notification bar
     */
    fun cancelNotification() {
        notificationManager.cancel(SERVICE_NOTIFICATION_ID)
    }
}