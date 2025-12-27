package com.volumebuttonfix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

/**
 * MainActivity - The main screen of the app
 *
 * This is the first screen users see when they open the app.
 * It provides:
 * - A button to start/stop the floating button service
 * - Status information about the service
 * - Access to settings
 * - Permission request handling
 *
 * For beginners: An Activity is like one screen in your app.
 * MainActivity is the "home screen" of our app.
 */
class MainActivity : AppCompatActivity() {

    // UI elements - these will be connected to the XML layout
    private lateinit var startServiceButton: Button
    private lateinit var settingsButton: Button
    private lateinit var statusText: TextView
    private lateinit var permissionHelper: PermissionHelper

    /**
     * onCreate - Called when the activity is first created
     * This is where we set up the UI and initialize components
     *
     * @param savedInstanceState - Saved state from previous instance (if app was closed)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load the layout from XML file
        setContentView(R.layout.activity_main)

        // Initialize the permission helper
        permissionHelper = PermissionHelper(this)

        // Connect UI elements to their XML counterparts by ID
        initializeViews()

        // Set up button click listeners
        setupButtons()

        // Update the status display
        updateServiceStatus()
    }

    /**
     * onResume - Called when the activity becomes visible to the user
     * We update the status here in case permissions changed while app was in background
     */
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    /**
     * Initialize all UI elements
     * This connects the Kotlin variables to the XML layout views
     */
    private fun initializeViews() {
        startServiceButton = findViewById(R.id.startServiceButton)
        settingsButton = findViewById(R.id.settingsButton)
        statusText = findViewById(R.id.statusText)
    }

    /**
     * Set up click listeners for all buttons
     * Defines what happens when user taps each button
     */
    private fun setupButtons() {
        // Start/Stop Service Button
        startServiceButton.setOnClickListener {
            handleServiceButtonClick()
        }

        // Settings Button
        settingsButton.setOnClickListener {
            // Open the settings screen
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Handle the start/stop service button click
     * This checks permissions and starts/stops the service accordingly
     */
    private fun handleServiceButtonClick() {
        if (isServiceRunning()) {
            // Service is running, so stop it
            stopOverlayService()
        } else {
            // Service is not running, check permissions first
            checkPermissionsAndStartService()
        }
    }

    /**
     * Check if all required permissions are granted
     * If not, request them. If yes, start the service.
     */
    private fun checkPermissionsAndStartService() {
        // Check overlay permission (required to draw floating buttons)
        if (!permissionHelper.hasOverlayPermission()) {
            showPermissionExplanationDialog(
                title = "Overlay Permission Required",
                message = "This app needs permission to display floating buttons over other apps. " +
                        "This is the core functionality of the app.\n\n" +
                        "On the next screen, please enable 'Display over other apps'.",
                onPositive = {
                    permissionHelper.requestOverlayPermission()
                }
            )
            return
        }

        // Check device admin permission (required to lock screen)
        if (!permissionHelper.isDeviceAdminEnabled()) {
            showPermissionExplanationDialog(
                title = "Device Administrator Required",
                message = "To lock your screen with the floating button, this app needs " +
                        "Device Administrator permission.\n\n" +
                        "On the next screen, please tap 'Activate' to enable screen locking.",
                onPositive = {
                    permissionHelper.requestDeviceAdminPermission()
                }
            )
            return
        }

        // Check notification permission (Android 13+)
        if (!permissionHelper.hasNotificationPermission()) {
            permissionHelper.requestNotificationPermission()
            // Note: This is handled by system permission dialog
            return
        }

        // All permissions granted, start the service
        startOverlayService()
    }

    /**
     * Show a dialog explaining why a permission is needed
     * Helps users understand what they're granting
     *
     * @param title - Dialog title
     * @param message - Explanation message
     * @param onPositive - Action to take when user taps "OK"
     */
    private fun showPermissionExplanationDialog(
        title: String,
        message: String,
        onPositive: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onPositive()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Permission required to start service",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Start the overlay service
     * Creates and starts the background service that displays floating buttons
     */
    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        // Use startForegroundService for Android 8.0+
        startForegroundService(intent)

        // Update UI
        updateServiceStatus()

        Toast.makeText(
            this,
            "Floating buttons activated!",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Stop the overlay service
     * Stops the background service and removes floating buttons
     */
    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)

        // Update UI
        updateServiceStatus()

        Toast.makeText(
            this,
            "Floating buttons deactivated",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Check if the overlay service is currently running
     *
     * @return true if service is active, false otherwise
     */
    private fun isServiceRunning(): Boolean {
        // Check if OverlayService is in the list of running services
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (OverlayService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Update the UI to show current service status
     * Changes button text and status message based on service state
     */
    private fun updateServiceStatus() {
        val isRunning = isServiceRunning()

        if (isRunning) {
            // Service is running
            startServiceButton.text = "Stop Floating Buttons"
            statusText.text = "Status: Active ✓\n\nFloating buttons are visible on your screen."
            startServiceButton.setBackgroundColor(getColor(R.color.red_500))
        } else {
            // Service is stopped
            startServiceButton.text = "Start Floating Buttons"
            statusText.text = "Status: Inactive\n\nTap the button below to activate floating buttons."
            startServiceButton.setBackgroundColor(getColor(R.color.primary))
        }
    }

    /**
     * Handle permission request results
     * Called when user returns from permission settings
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PermissionHelper.OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (permissionHelper.hasOverlayPermission()) {
                    Toast.makeText(
                        this,
                        "Overlay permission granted!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Try to start service again (will check next permission)
                    checkPermissionsAndStartService()
                } else {
                    Toast.makeText(
                        this,
                        "Overlay permission denied. App cannot function without it.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            PermissionHelper.DEVICE_ADMIN_REQUEST_CODE -> {
                if (permissionHelper.isDeviceAdminEnabled()) {
                    Toast.makeText(
                        this,
                        "Device admin enabled! Screen lock will work.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Try to start service again (will check next permission)
                    checkPermissionsAndStartService()
                } else {
                    Toast.makeText(
                        this,
                        "Device admin not enabled. Screen lock button won't work.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Allow starting service even without device admin
                    // (other buttons will still work)
                    if (permissionHelper.hasOverlayPermission()) {
                        startOverlayService()
                    }
                }
            }
        }
    }

    /**
     * Handle runtime permission results (for Android 13+ notifications)
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (permissionHelper.hasNotificationPermission()) {
                // Permission granted, try starting service
                checkPermissionsAndStartService()
            } else {
                // Permission denied, but service can still work
                // (notifications just won't show)
                Toast.makeText(
                    this,
                    "Notification permission denied. Service will still work.",
                    Toast.LENGTH_SHORT
                ).show()
                if (permissionHelper.hasOverlayPermission() &&
                    permissionHelper.isDeviceAdminEnabled()) {
                    startOverlayService()
                }
            }
        }
    }
}