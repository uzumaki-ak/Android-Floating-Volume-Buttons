package com.volumebuttonfix

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import kotlin.math.abs

/**
 * ULTIMATE FIX - Click vs Drag properly separated, no right side barrier
 */
class FloatingButtonView(
    private val context: Context,
    private val windowManager: WindowManager,
    private val volumeController: VolumeController,
    private val screenLockHelper: ScreenLockHelper,
    private val onRemove: () -> Unit
) {

    // Managers and Controllers
    private val prefsManager = PreferencesManager(context)
    private val brightnessController = BrightnessController(context)
    private val connectivityController = ConnectivityController(context)
    private val usageStatsManager = UsageStatsManager(context)
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Views
    private var collapsedView: View? = null
    private var expandedView: View? = null
    private var currentView: View? = null

    // Buttons
    private val buttonMap = mutableMapOf<PreferencesManager.ButtonType, ImageButton>()
    private var menuButton: ImageButton? = null
    private var collapseButton: ImageButton? = null

    // Layout parameters
    private var layoutParams: WindowManager.LayoutParams? = null

    // Drag state
    private var isDragging = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var dragStartTime = 0L
    private val dragThreshold = 20 // pixels
    private val clickThreshold = 300L // ms (max time for a click)
    private val clickMoveThreshold = 30 // pixels (max movement for a click)

    // Auto-hide
    private val autoHideHandler = Handler(Looper.getMainLooper())
    private val autoHideRunnable = Runnable { fadeOut() }

    // Screen dimensions
    private var screenWidth = 0
    private var screenHeight = 0
    private var statusBarHeight = 0

    /**
     * Add floating view to window
     */
    fun addToWindow() {
        // Get screen dimensions
        updateScreenDimensions()

        // Create layout params
        layoutParams = createLayoutParams()

        // Create views
        createCollapsedView()
        createExpandedView()

        // Show appropriate view
        if (prefsManager.isCollapsed()) {
            showCollapsed()
        } else {
            showExpanded()
        }

        // Start auto-hide if enabled
        if (prefsManager.isAutoHideEnabled()) {
            startAutoHideTimer()
        }
    }

    /**
     * Update screen dimensions
     */
    private fun updateScreenDimensions() {
        val metrics = context.resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        // Get status bar height
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // ← THIS IS KEY! Removes boundaries
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START

            // Load saved position
            var savedX = prefsManager.getPositionX()
            var savedY = prefsManager.getPositionY()

            // If position is invalid, set default
            if (savedX == -1 || savedY == -1) {
                savedX = 50
                savedY = screenHeight / 2
                prefsManager.setPosition(savedX, savedY)
            }

            x = savedX
            y = savedY

            // Remove any margins/padding
            horizontalMargin = 0f
            verticalMargin = 0f
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createCollapsedView() {
        collapsedView = LayoutInflater.from(context)
            .inflate(R.layout.floating_button_collapsed, null)

        menuButton = collapsedView?.findViewById(R.id.menuButton)
        applyCustomization(menuButton)

        // FIX: Setup BOTH click and drag on the same button
        setupCollapsedButtonWithClickAndDrag()
    }

    /**
     * FIX: This is the KEY function - separates CLICK vs DRAG
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupCollapsedButtonWithClickAndDrag() {
        var hasMoved = false
        var clickAction = Runnable {
            if (!hasMoved && !isDragging) {
                performHapticFeedback()
                showExpanded()
            }
        }

        menuButton?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Record start time and position
                    dragStartTime = System.currentTimeMillis()
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    hasMoved = false
                    isDragging = false

                    // Schedule click action (will be cancelled if drag occurs)
                    v.postDelayed(clickAction, clickThreshold)
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY

                    // Check if movement exceeds threshold
                    if (abs(deltaX) > dragThreshold || abs(deltaY) > dragThreshold) {
                        hasMoved = true

                        // Cancel the pending click
                        v.removeCallbacks(clickAction)

                        if (!isDragging) {
                            isDragging = true
                            performHapticFeedback() // Gentle feedback when drag starts
                        }

                        // DRAG THE VIEW
                        layoutParams?.let { params ->
                            params.x = initialX + deltaX.toInt()
                            params.y = initialY + deltaY.toInt()

                            // FIX: Allow dragging BEYOND screen edges (FLAG_LAYOUT_NO_LIMITS allows this)
                            // But we'll keep it within reasonable bounds
                            val buttonWidth = v.measuredWidth
                            val buttonHeight = v.measuredHeight

                            // Calculate boundaries (with small margin for usability)
                            val minX = -buttonWidth / 2  // Allow half off-screen on left
                            val maxX = screenWidth - buttonWidth / 2  // Allow half off-screen on right
                            val minY = -statusBarHeight  // Allow above status bar
                            val maxY = screenHeight - buttonHeight / 2  // Allow half off-screen on bottom

                            // Apply boundaries
                            params.x = params.x.coerceIn(minX, maxX)
                            params.y = params.y.coerceIn(minY, maxY)

                            try {
                                windowManager.updateViewLayout(collapsedView, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return@setOnTouchListener true
                    }
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    // Remove pending click action
                    v.removeCallbacks(clickAction)

                    if (isDragging) {
                        // Save position
                        layoutParams?.let { params ->
                            prefsManager.setPosition(params.x, params.y)
                        }
                        isDragging = false
                        hasMoved = false
                        return@setOnTouchListener true
                    }

                    // If not dragged and within time limit, execute click
                    if (!hasMoved && (System.currentTimeMillis() - dragStartTime) < clickThreshold) {
                        clickAction.run()
                    }

                    hasMoved = false
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.removeCallbacks(clickAction)
                    hasMoved = false
                    isDragging = false
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createExpandedView() {
        expandedView = LayoutInflater.from(context)
            .inflate(R.layout.floating_button_layout, null)

        // Get the scroll view
        val scrollView = expandedView?.findViewById<ScrollView>(R.id.scrollView)

        // Initialize all buttons
        initializeAllButtons()

        // Setup collapse button
        setupCollapseButton()

        // Setup drag for expanded view
        setupExpandedViewDrag(scrollView)
    }

    /**
     * Setup collapse button
     */
    private fun setupCollapseButton() {
        collapseButton = expandedView?.findViewById(R.id.collapseButton)
        applyCustomization(collapseButton)

        // Clear and set click listener
        collapseButton?.setOnClickListener(null)
        collapseButton?.setOnClickListener {
            performHapticFeedback()
            showCollapsed()
        }
    }

    /**
     * Setup drag for expanded view
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupExpandedViewDrag(scrollView: ScrollView?) {
        // Get the buttons layout
        val buttonsLayout = expandedView?.findViewById<View>(R.id.buttonsLayout)

        // Variable to track if we're scrolling or dragging
        var isScrolling = false

        // Setup on the main expanded view
        expandedView?.setOnTouchListener { v, event ->
            handleExpandedViewDrag(v, event)
        }

        // Setup on scroll view with better scroll/drag detection
        scrollView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isScrolling = false
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = abs(event.rawY - initialTouchY)

                    // If mostly horizontal movement, it's a drag
                    if (deltaX > deltaY * 1.5 && deltaX > dragThreshold) {
                        // It's a drag, handle it
                        return@setOnTouchListener handleExpandedViewDrag(v, event)
                    } else if (deltaY > dragThreshold) {
                        // It's a scroll
                        isScrolling = true
                    }
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP -> {
                    isScrolling = false
                    return@setOnTouchListener false
                }
            }
            false
        }

        // Also setup on buttons layout
        buttonsLayout?.setOnTouchListener { v, event ->
            if (!isScrolling) {
                handleExpandedViewDrag(v, event)
            } else {
                false
            }
        }
    }

    /**
     * Handle drag for expanded view
     */
    private fun handleExpandedViewDrag(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams?.x ?: 0
                initialY = layoutParams?.y ?: 0
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY

                if (!isDragging && (abs(deltaX) > dragThreshold || abs(deltaY) > dragThreshold)) {
                    isDragging = true
                    performHapticFeedback() // Feedback when drag starts
                }

                if (isDragging) {
                    layoutParams?.let { params ->
                        params.x = initialX + deltaX.toInt()
                        params.y = initialY + deltaY.toInt()

                        // Get view dimensions
                        val viewWidth = expandedView?.width ?: 300
                        val viewHeight = expandedView?.height ?: 600

                        // Calculate boundaries (allow partial off-screen)
                        val minX = -viewWidth / 2
                        val maxX = screenWidth - viewWidth / 2
                        val minY = -statusBarHeight
                        val maxY = screenHeight - viewHeight / 2

                        params.x = params.x.coerceIn(minX, maxX)
                        params.y = params.y.coerceIn(minY, maxY)

                        try {
                            windowManager.updateViewLayout(expandedView, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    return true
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    layoutParams?.let { params ->
                        prefsManager.setPosition(params.x, params.y)
                    }
                    isDragging = false
                    return true
                }
                return false
            }
        }
        return false
    }

    private fun initializeAllButtons() {
        initializeButton(PreferencesManager.ButtonType.VOLUME_UP, R.id.volumeUpButton)
        initializeButton(PreferencesManager.ButtonType.VOLUME_DOWN, R.id.volumeDownButton)
        initializeButton(PreferencesManager.ButtonType.MUTE, R.id.muteButton)
        initializeButton(PreferencesManager.ButtonType.LOCK, R.id.lockButton)
        initializeButton(PreferencesManager.ButtonType.BRIGHTNESS_UP, R.id.brightnessUpButton)
        initializeButton(PreferencesManager.ButtonType.BRIGHTNESS_DOWN, R.id.brightnessDownButton)
        initializeButton(PreferencesManager.ButtonType.HOME, R.id.homeButton)
        initializeButton(PreferencesManager.ButtonType.RECENT_APPS, R.id.recentAppsButton)
        initializeButton(PreferencesManager.ButtonType.FLASHLIGHT, R.id.flashlightButton)
        initializeButton(PreferencesManager.ButtonType.WIFI, R.id.wifiButton)
        initializeButton(PreferencesManager.ButtonType.BLUETOOTH, R.id.bluetoothButton)
    }

    private fun initializeButton(type: PreferencesManager.ButtonType, viewId: Int) {
        val button = expandedView?.findViewById<ImageButton>(viewId)
        button?.let {
            buttonMap[type] = it
            applyCustomization(it)

            it.setOnClickListener {
                performHapticFeedback()
                handleButtonClick(type)
                recordUsage(type)
                resetAutoHideTimer()
            }

            if (prefsManager.isButtonEnabled(type)) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    private fun handleButtonClick(type: PreferencesManager.ButtonType) {
        when (type) {
            PreferencesManager.ButtonType.VOLUME_UP -> volumeController.increaseVolume()
            PreferencesManager.ButtonType.VOLUME_DOWN -> volumeController.decreaseVolume()
            PreferencesManager.ButtonType.MUTE -> volumeController.toggleMute()
            PreferencesManager.ButtonType.LOCK -> screenLockHelper.lockScreen()
            PreferencesManager.ButtonType.BRIGHTNESS_UP -> {
                if (!brightnessController.hasPermission()) {
                    requestBrightnessPermission()
                } else {
                    brightnessController.increaseBrightness()
                }
            }
            PreferencesManager.ButtonType.BRIGHTNESS_DOWN -> {
                if (!brightnessController.hasPermission()) {
                    requestBrightnessPermission()
                } else {
                    brightnessController.decreaseBrightness()
                }
            }
            PreferencesManager.ButtonType.HOME -> connectivityController.goHome()
            PreferencesManager.ButtonType.RECENT_APPS -> connectivityController.openRecentApps()
            PreferencesManager.ButtonType.FLASHLIGHT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(android.Manifest.permission.CAMERA) !=
                        android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        requestCameraPermission()
                        return
                    }
                }
                connectivityController.toggleFlashlight()
            }
            PreferencesManager.ButtonType.WIFI -> connectivityController.toggleWifi()
            PreferencesManager.ButtonType.BLUETOOTH -> connectivityController.toggleBluetooth()
        }
    }

    private fun requestBrightnessPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                Toast.makeText(
                    context,
                    "Please enable 'Modify system settings' permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestCameraPermission() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

            Toast.makeText(
                context,
                "Please enable Camera permission for flashlight",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyCustomization(button: ImageButton?) {
        button?.let {
            val sizeDp = prefsManager.getButtonSizeDp()
            val sizePixels = (sizeDp * context.resources.displayMetrics.density).toInt()
            it.layoutParams.width = sizePixels
            it.layoutParams.height = sizePixels
            it.alpha = prefsManager.getTransparencyAlpha()
            val colorValue = prefsManager.getColorThemeValue(context)
            it.background?.setTint(colorValue)
        }
    }

    private fun showCollapsed() {
        try {
            // Remove expanded view if exists
            try {
                expandedView?.let { windowManager.removeView(it) }
            } catch (e: Exception) {
                // View might not be attached
            }

            // Add collapsed view
            windowManager.addView(collapsedView, layoutParams)
            currentView = collapsedView
            prefsManager.setCollapsed(true)

            // Start auto-hide timer
            if (prefsManager.isAutoHideEnabled()) {
                startAutoHideTimer()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showExpanded() {
        try {
            // Remove collapsed view if exists
            try {
                collapsedView?.let { windowManager.removeView(it) }
            } catch (e: Exception) {
                // View might not be attached
            }

            // Add expanded view
            windowManager.addView(expandedView, layoutParams)
            currentView = expandedView
            prefsManager.setCollapsed(false)

            // Reset auto-hide timer
            resetAutoHideTimer()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshSettings() {
        updateScreenDimensions()

        // Save current position
        val currentX = layoutParams?.x ?: 50
        val currentY = layoutParams?.y ?: 500

        // Recreate views
        createCollapsedView()
        createExpandedView()

        // Re-add appropriate view
        try {
            currentView?.let {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            if (prefsManager.isCollapsed()) {
                showCollapsed()
            } else {
                showExpanded()
            }

            // Restore position
            layoutParams?.x = currentX
            layoutParams?.y = currentY

            currentView?.let {
                windowManager.updateViewLayout(it, layoutParams)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // If refresh fails, restart
            removeFromWindow()
            addToWindow()
        }
    }

    private fun recordUsage(type: PreferencesManager.ButtonType) {
        usageStatsManager.recordButtonPress(type)
    }

    private fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startAutoHideTimer() {
        if (prefsManager.isAutoHideEnabled()) {
            autoHideHandler.postDelayed(autoHideRunnable, prefsManager.getAutoHideDelay())
        }
    }

    private fun resetAutoHideTimer() {
        if (prefsManager.isAutoHideEnabled()) {
            autoHideHandler.removeCallbacks(autoHideRunnable)
            fadeIn()
            startAutoHideTimer()
        }
    }

    private fun fadeOut() {
        currentView?.animate()?.alpha(0.3f)?.setDuration(300)?.start()
    }

    private fun fadeIn() {
        currentView?.animate()?.alpha(prefsManager.getTransparencyAlpha())?.setDuration(300)?.start()
    }

    fun removeFromWindow() {
        try {
            autoHideHandler.removeCallbacks(autoHideRunnable)
            connectivityController.cleanup()

            currentView?.let { view ->
                try {
                    windowManager.removeView(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            currentView = null
            collapsedView = null
            expandedView = null
        }
    }
}