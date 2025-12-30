package com.volumebuttonfix

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * FIXED SettingsActivity - Settings now apply in real-time!
 *
 * Path: app/src/main/java/com/volumebuttonfix/SettingsActivity.kt
 * Action: REPLACE ENTIRE FILE
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var permissionHelper: PermissionHelper

    private val buttonSwitches = mutableMapOf<PreferencesManager.ButtonType, SwitchMaterial>()
    private lateinit var sizeRadioGroup: RadioGroup
    private lateinit var transparencySeekBar: SeekBar
    private lateinit var transparencyText: TextView
    private lateinit var colorSpinner: Spinner
    private lateinit var autoHideSwitch: SwitchMaterial
    private lateinit var autoHideDelaySeekBar: SeekBar
    private lateinit var autoHideDelayText: TextView
    private lateinit var gesturesSwitch: SwitchMaterial
    private lateinit var statsText: TextView
    private lateinit var resetStatsButton: Button
    private lateinit var managePermissionsButton: Button
    private lateinit var positionButtonsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        prefsManager = PreferencesManager(this)
        usageStatsManager = UsageStatsManager(this)
        permissionHelper = PermissionHelper(this)

        initializeViews()
        loadSettings()
        setupListeners()
        updateStatistics()
    }

    private fun initializeViews() {
        buttonSwitches[PreferencesManager.ButtonType.VOLUME_UP] = findViewById(R.id.switchVolumeUp)
        buttonSwitches[PreferencesManager.ButtonType.VOLUME_DOWN] = findViewById(R.id.switchVolumeDown)
        buttonSwitches[PreferencesManager.ButtonType.MUTE] = findViewById(R.id.switchMute)
        buttonSwitches[PreferencesManager.ButtonType.LOCK] = findViewById(R.id.switchLock)
        buttonSwitches[PreferencesManager.ButtonType.BRIGHTNESS_UP] = findViewById(R.id.switchBrightnessUp)
        buttonSwitches[PreferencesManager.ButtonType.BRIGHTNESS_DOWN] = findViewById(R.id.switchBrightnessDown)
        buttonSwitches[PreferencesManager.ButtonType.HOME] = findViewById(R.id.switchHome)
        buttonSwitches[PreferencesManager.ButtonType.RECENT_APPS] = findViewById(R.id.switchRecentApps)
        buttonSwitches[PreferencesManager.ButtonType.FLASHLIGHT] = findViewById(R.id.switchFlashlight)
        buttonSwitches[PreferencesManager.ButtonType.WIFI] = findViewById(R.id.switchWifi)
        buttonSwitches[PreferencesManager.ButtonType.BLUETOOTH] = findViewById(R.id.switchBluetooth)

        sizeRadioGroup = findViewById(R.id.sizeRadioGroup)
        transparencySeekBar = findViewById(R.id.transparencySeekBar)
        transparencyText = findViewById(R.id.transparencyText)
        colorSpinner = findViewById(R.id.colorSpinner)
        autoHideSwitch = findViewById(R.id.autoHideSwitch)
        autoHideDelaySeekBar = findViewById(R.id.autoHideDelaySeekBar)
        autoHideDelayText = findViewById(R.id.autoHideDelayText)
        gesturesSwitch = findViewById(R.id.gesturesSwitch)
        statsText = findViewById(R.id.statsText)
        resetStatsButton = findViewById(R.id.resetStatsButton)
        managePermissionsButton = findViewById(R.id.managePermissionsButton)
        positionButtonsLayout = findViewById(R.id.positionButtonsLayout)
    }

    private fun loadSettings() {
        buttonSwitches.forEach { (type, switch) ->
            switch.isChecked = prefsManager.isButtonEnabled(type)
        }

        when (prefsManager.getButtonSize()) {
            0 -> findViewById<RadioButton>(R.id.sizeSmall).isChecked = true
            1 -> findViewById<RadioButton>(R.id.sizeMedium).isChecked = true
            2 -> findViewById<RadioButton>(R.id.sizeLarge).isChecked = true
        }

        transparencySeekBar.progress = prefsManager.getTransparency()
        transparencyText.text = "${prefsManager.getTransparency()}%"

        colorSpinner.setSelection(prefsManager.getColorTheme())

        autoHideSwitch.isChecked = prefsManager.isAutoHideEnabled()
        val delaySeconds = (prefsManager.getAutoHideDelay() / 1000).toInt()
        autoHideDelaySeekBar.progress = delaySeconds
        autoHideDelayText.text = "$delaySeconds seconds"
        autoHideDelaySeekBar.isEnabled = autoHideSwitch.isChecked

        gesturesSwitch.isChecked = prefsManager.isGesturesEnabled()
    }

    private fun setupListeners() {
        // FIXED: Settings now apply in real-time!
        buttonSwitches.forEach { (type, switch) ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                prefsManager.setButtonEnabled(type, isChecked)
                notifySettingsChanged() // Auto-apply!
            }
        }

        sizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val size = when (checkedId) {
                R.id.sizeSmall -> 0
                R.id.sizeLarge -> 2
                else -> 1
            }
            prefsManager.setButtonSize(size)
            notifySettingsChanged() // Auto-apply!
        }

        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceIn(50, 100)
                transparencyText.text = "$value%"
                if (fromUser) {
                    prefsManager.setTransparency(value)
                    notifySettingsChanged() // Auto-apply!
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefsManager.setColorTheme(position)
                notifySettingsChanged() // Auto-apply!
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        autoHideSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setAutoHideEnabled(isChecked)
            autoHideDelaySeekBar.isEnabled = isChecked
            notifySettingsChanged() // Auto-apply!
        }

        autoHideDelaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seconds = progress.coerceIn(3, 60)
                autoHideDelayText.text = "$seconds seconds"
                if (fromUser) {
                    prefsManager.setAutoHideDelay(seconds * 1000L)
                    notifySettingsChanged() // Auto-apply!
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        gesturesSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setGesturesEnabled(isChecked)
            notifySettingsChanged() // Auto-apply!
        }

        resetStatsButton.setOnClickListener {
            usageStatsManager.resetStats()
            updateStatistics()
            Toast.makeText(this, "Statistics reset", Toast.LENGTH_SHORT).show()
        }

        managePermissionsButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                permissionHelper.openAppSettings()
            }
        }

        setupPositionPresets()
    }

    private fun setupPositionPresets() {
        findViewById<Button>(R.id.positionTopLeft).setOnClickListener {
            prefsManager.setPosition(50, 100)
            notifySettingsChanged()
        }
        findViewById<Button>(R.id.positionTopRight).setOnClickListener {
            val screenWidth = resources.displayMetrics.widthPixels
            prefsManager.setPosition(screenWidth - 150, 100)
            notifySettingsChanged()
        }
        findViewById<Button>(R.id.positionBottomLeft).setOnClickListener {
            val screenHeight = resources.displayMetrics.heightPixels
            prefsManager.setPosition(50, screenHeight - 500)
            notifySettingsChanged()
        }
        findViewById<Button>(R.id.positionBottomRight).setOnClickListener {
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            prefsManager.setPosition(screenWidth - 150, screenHeight - 500)
            notifySettingsChanged()
        }
        findViewById<Button>(R.id.positionCenterLeft).setOnClickListener {
            val screenHeight = resources.displayMetrics.heightPixels
            prefsManager.setPosition(50, screenHeight / 2)
            notifySettingsChanged()
        }
        findViewById<Button>(R.id.positionCenterRight).setOnClickListener {
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            prefsManager.setPosition(screenWidth - 150, screenHeight / 2)
            notifySettingsChanged()
        }
    }

    /**
     * FIXED: Notify service of settings changes for real-time update
     */
    private fun notifySettingsChanged() {
        val intent = Intent(OverlayService.ACTION_SETTINGS_CHANGED)
        sendBroadcast(intent)

        Toast.makeText(
            this,
            "Settings applied ✓",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateStatistics() {
        val summary = usageStatsManager.getStatsSummary()
        statsText.text = summary
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}