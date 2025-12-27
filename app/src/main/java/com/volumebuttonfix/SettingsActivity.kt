package com.volumebuttonfix

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * SettingsActivity - App settings and configuration screen
 *
 * This screen shows app information and settings:
 * - Permission status
 * - App version
 * - Option to manage permissions
 * - About information
 *
 * For beginners: This is a simple screen that shows useful information
 * and lets users manage app permissions.
 */
class SettingsActivity : AppCompatActivity() {

    // UI elements
    private lateinit var overlayStatusText: TextView
    private lateinit var deviceAdminStatusText: TextView
    private lateinit var notificationStatusText: TextView
    private lateinit var managePermissionsButton: Button

    // Permission helper
    private lateinit var permissionHelper: PermissionHelper

    /**
     * onCreate - Called when activity is created
     * Set up the UI and display current status
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize permission helper
        permissionHelper = PermissionHelper(this)

        // Initialize UI elements
        initializeViews()

        // Set up button click listeners
        setupButtons()

        // Update permission status display
        updatePermissionStatus()
    }

    /**
     * onResume - Called when activity becomes visible
     * Update status in case permissions changed in background
     */
    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    /**
     * Initialize all UI elements
     */
    private fun initializeViews() {
        overlayStatusText = findViewById(R.id.overlayStatusText)
        deviceAdminStatusText = findViewById(R.id.deviceAdminStatusText)
        notificationStatusText = findViewById(R.id.notificationStatusText)
        managePermissionsButton = findViewById(R.id.managePermissionsButton)
    }

    /**
     * Set up button click listeners
     */
    private fun setupButtons() {
        // Manage Permissions Button - opens app settings
        managePermissionsButton.setOnClickListener {
            permissionHelper.openAppSettings()
        }
    }

    /**
     * Update the permission status display
     * Shows whether each permission is granted or not
     */
    private fun updatePermissionStatus() {
        // Overlay Permission Status
        val overlayGranted = permissionHelper.hasOverlayPermission()
        overlayStatusText.text = buildString {
            append("Overlay Permission: ")
            append(if (overlayGranted) "✓ Granted" else "✗ Not Granted")
            append("\n\n")
            append("Allows floating buttons to appear over other apps.")
        }
        overlayStatusText.setTextColor(
            if (overlayGranted) getColor(R.color.green_500)
            else getColor(R.color.red_500)
        )

        // Device Admin Status
        val deviceAdminEnabled = permissionHelper.isDeviceAdminEnabled()
        deviceAdminStatusText.text = buildString {
            append("Device Administrator: ")
            append(if (deviceAdminEnabled) "✓ Enabled" else "✗ Not Enabled")
            append("\n\n")
            append("Required to lock the screen with the floating button.")
        }
        deviceAdminStatusText.setTextColor(
            if (deviceAdminEnabled) getColor(R.color.green_500)
            else getColor(R.color.orange_500)
        )

        // Notification Permission Status
        val notificationGranted = permissionHelper.hasNotificationPermission()
        notificationStatusText.text = buildString {
            append("Notification Permission: ")
            append(if (notificationGranted) "✓ Granted" else "✗ Not Granted")
            append("\n\n")
            append("Required for the service notification.")
        }
        notificationStatusText.setTextColor(
            if (notificationGranted) getColor(R.color.green_500)
            else getColor(R.color.orange_500)
        )
    }

    /**
     * Handle action bar back button press
     * Returns to previous screen (MainActivity)
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}