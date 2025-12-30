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
 * UPDATED PermissionHelper - Now handles all new permissions
 *
 * Path: app/src/main/java/com/volumebuttonfix/PermissionHelper.kt
 * Action: REPLACE ENTIRE FILE
 */
class PermissionHelper(private val activity: Activity) {

    private val context: Context = activity.applicationContext

    companion object {
        const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
        const val DEVICE_ADMIN_REQUEST_CODE = 1002
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
        const val BRIGHTNESS_PERMISSION_REQUEST_CODE = 1004
        const val CAMERA_PERMISSION_REQUEST_CODE = 1005
    }

    /**
     * Check if overlay permission is granted
     */
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Request overlay permission
     */
    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    /**
     * Check if device administrator is enabled
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
     */
    fun requestDeviceAdminPermission() {
        try {
            val componentName = ComponentName(
                context,
                ScreenLockHelper.DeviceAdminReceiver::class.java
            )

            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "This app needs Device Administrator permission to lock your screen"
                )
            }

            activity.startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Request notification permission (Android 13+)
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
     * Check if brightness permission is granted (Android 6+)
     */
    fun hasBrightnessPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    /**
     * Request brightness permission (Android 6+)
     */
    fun requestBrightnessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            activity.startActivityForResult(intent, BRIGHTNESS_PERMISSION_REQUEST_CODE)
        }
    }

    /**
     * Check if camera permission is granted (for flashlight)
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request camera permission (for flashlight)
     */
    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllPermissions(): Boolean {
        return hasOverlayPermission() &&
                isDeviceAdminEnabled() &&
                hasNotificationPermission()
    }

    /**
     * Open app settings page
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        activity.startActivity(intent)
    }
}