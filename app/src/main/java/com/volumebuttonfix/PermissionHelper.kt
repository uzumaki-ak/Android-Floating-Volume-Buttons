package com.volumebuttonfix

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionHelper - Manages all app permissions
 *
 * This class handles checking and requesting various permissions:
 * - Overlay permission (to draw floating buttons)
 * - Device Admin (to lock screen)
 * - Notification permission (for foreground service notification)
 *
 * For beginners: Android apps need explicit permission from users
 * to access sensitive features. This class makes it easy to manage those permissions.
 */
class PermissionHelper(private val activity: Activity) {

    // Context reference for system services
    private val context: Context = activity.applicationContext

    companion object {
        // Request codes - unique IDs for different permission requests
        const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
        const val DEVICE_ADMIN_REQUEST_CODE = 1002
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
    }

    /**
     * Check if overlay permission is granted
     * Overlay permission allows drawing over other apps
     *
     * @return true if permission granted, false otherwise
     */
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ requires explicit overlay permission
            Settings.canDrawOverlays(context)
        } else {
            // Older Android versions don't need this permission
            true
        }
    }

    /**
     * Request overlay permission from user
     * Opens system settings where user can grant the permission
     */
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create intent to open overlay permission settings
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            // Start the settings activity
            // Result will be returned to onActivityResult in MainActivity
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    /**
     * Check if device administrator is enabled
     * Device admin is required to lock the screen programmatically
     *
     * @return true if device admin is active, false otherwise
     */
    fun isDeviceAdminEnabled(): Boolean {
        return try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
                    as DevicePolicyManager
            val componentName = ComponentName(
                context,
                ScreenLockHelper.DeviceAdminReceiver::class.java
            )
            devicePolicyManager.isAdminActive(componentName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Request device administrator permission
     * Opens system dialog asking user to grant admin rights
     */
    fun requestDeviceAdminPermission() {
        try {
            val componentName = ComponentName(
                context,
                ScreenLockHelper.DeviceAdminReceiver::class.java
            )

            // Create intent to request device admin
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "This app needs Device Administrator permission to lock your screen"
                )
            }

            // Start the device admin request activity
            activity.startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     * Required to show persistent notification for foreground service
     *
     * @return true if permission granted or not required, false otherwise
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires notification permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Older versions don't need notification permission
            true
        }
    }

    /**
     * Request notification permission (Android 13+)
     * Shows system dialog asking for notification permission
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Check if all required permissions are granted
     *
     * @return true if all permissions granted, false otherwise
     */
    fun hasAllPermissions(): Boolean {
        return hasOverlayPermission() &&
                isDeviceAdminEnabled() &&
                hasNotificationPermission()
    }

    /**
     * Open app settings page
     * Useful if user needs to manually manage permissions
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        activity.startActivity(intent)
    }
}