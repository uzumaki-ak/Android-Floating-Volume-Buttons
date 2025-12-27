package com.volumebuttonfix

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * ScreenLockHelper - Handles screen lock functionality
 *
 * This class manages locking the device screen programmatically.
 * It requires Device Administrator permission to work.
 *
 * For beginners: Device Administrator is a special permission that allows
 * apps to perform system-level actions like locking the screen.
 */
class ScreenLockHelper(private val context: Context) {

    // DevicePolicyManager - System service for device administration
    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    // Component name for our DeviceAdminReceiver
    private val componentName: ComponentName =
        ComponentName(context, DeviceAdminReceiver::class.java)

    /**
     * Lock the device screen
     * This is equivalent to pressing the physical power button
     */
    fun lockScreen() {
        try {
            if (isDeviceAdminEnabled()) {
                // Lock the screen immediately
                devicePolicyManager.lockNow()
            } else {
                // Device admin not enabled
                Toast.makeText(
                    context,
                    "Screen lock requires Device Administrator permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to lock screen",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Check if Device Administrator is enabled for this app
     *
     * @return true if enabled, false otherwise
     */
    fun isDeviceAdminEnabled(): Boolean {
        return try {
            devicePolicyManager.isAdminActive(componentName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * DeviceAdminReceiver - Receiver that handles device admin events
     *
     * This is required by Android to grant Device Administrator permission.
     * It receives callbacks when admin status changes.
     *
     * For beginners: This is like a listener that Android uses to notify
     * your app about device admin events.
     */
    class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {

        /**
         * Called when device admin is enabled for this app
         */
        override fun onEnabled(context: Context, intent: Intent) {
            super.onEnabled(context, intent)
            Toast.makeText(
                context,
                "Device Administrator Enabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        /**
         * Called when device admin is disabled for this app
         */
        override fun onDisabled(context: Context, intent: Intent) {
            super.onDisabled(context, intent)
            Toast.makeText(
                context,
                "Device Administrator Disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        /**
         * Called when user tries to disable device admin
         * You can return a message explaining why it's needed
         */
        override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
            return "Device Administrator is required to lock the screen with floating buttons"
        }
    }
}