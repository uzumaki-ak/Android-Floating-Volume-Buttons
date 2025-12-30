package com.volumebuttonfix

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * ConnectivityController - FIXED with proper permission checks
 *
 * Path: app/src/main/java/com/volumebuttonfix/ConnectivityController.kt
 * Action: REPLACE ENTIRE FILE
 */
class ConnectivityController(private val context: Context) {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothManager?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var isFlashlightOn = false
    private var cameraId: String? = null

    init {
        // Get camera ID for flashlight
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Toggle WiFi on/off
     */
    fun toggleWifi() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Open WiFi settings instead
                val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                Toast.makeText(
                    context,
                    "Please toggle WiFi manually",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Android 9 and below - Direct toggle
                @Suppress("DEPRECATION")
                val wifiEnabled = wifiManager?.isWifiEnabled ?: false

                @Suppress("DEPRECATION")
                wifiManager?.isWifiEnabled = !wifiEnabled

                val status = if (wifiEnabled) "OFF" else "ON"
                Toast.makeText(
                    context,
                    "WiFi turned $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "WiFi permission required",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to toggle WiFi",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Toggle Bluetooth on/off
     */
    fun toggleBluetooth() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - Check permission first
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    Toast.makeText(
                        context,
                        "Bluetooth permission required",
                        Toast.LENGTH_SHORT
                    ).show()
                    openBluetoothSettings()
                    return
                }

                // Open settings on Android 12+
                openBluetoothSettings()
            } else {
                // Android 11 and below - Direct toggle
                val adapter = bluetoothAdapter
                if (adapter == null) {
                    Toast.makeText(
                        context,
                        "Bluetooth not available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                try {
                    @Suppress("DEPRECATION")
                    if (adapter.isEnabled) {
                        @Suppress("DEPRECATION")
                        adapter.disable()
                        Toast.makeText(context, "Bluetooth turned OFF", Toast.LENGTH_SHORT).show()
                    } else {
                        @Suppress("DEPRECATION")
                        adapter.enable()
                        Toast.makeText(context, "Bluetooth turned ON", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Bluetooth permission required",
                        Toast.LENGTH_SHORT
                    ).show()
                    openBluetoothSettings()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to toggle Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Open Bluetooth settings
     */
    private fun openBluetoothSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

            Toast.makeText(
                context,
                "Please toggle Bluetooth manually",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Toggle flashlight on/off
     */
    fun toggleFlashlight() {
        try {
            // Check camera permission
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Toast.makeText(
                    context,
                    "Camera permission required for flashlight",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val camId = cameraId
            if (camId == null) {
                Toast.makeText(
                    context,
                    "Flashlight not available",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            isFlashlightOn = !isFlashlightOn

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager?.setTorchMode(camId, isFlashlightOn)
            }

            val status = if (isFlashlightOn) "ON" else "OFF"
            Toast.makeText(
                context,
                "Flashlight turned $status",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: SecurityException) {
            e.printStackTrace()
            isFlashlightOn = false
            Toast.makeText(
                context,
                "Camera permission required",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            isFlashlightOn = false
            Toast.makeText(
                context,
                "Failed to toggle flashlight",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Go to home screen
     */
    fun goHome() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to go home",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Open recent apps (app switcher)
     */
    fun openRecentApps() {
        try {
            Toast.makeText(
                context,
                "Recent apps: Long press Home button or gesture",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cleanup when service is destroyed
     */
    fun cleanup() {
        // Turn off flashlight if it's on
        if (isFlashlightOn) {
            try {
                cameraId?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cameraManager?.setTorchMode(it, false)
                    }
                }
                isFlashlightOn = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}