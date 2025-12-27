package com.volumebuttonfix

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import kotlin.math.abs

/**
 * FloatingButtonView - The actual floating overlay with buttons
 *
 * This class creates and manages the floating button overlay that appears on screen.
 * It handles:
 * - Creating the overlay window
 * - Positioning the buttons
 * - Drag functionality
 * - Button clicks
 * - Haptic feedback
 *
 * For beginners: This is the visual part that users see and interact with.
 * It's like a small window floating over all other apps.
 */
class FloatingButtonView(
    private val context: Context,
    private val windowManager: WindowManager,
    private val volumeController: VolumeController,
    private val screenLockHelper: ScreenLockHelper,
    private val onRemove: () -> Unit
) {

    // The root view that contains all buttons
    private var floatingView: View? = null

    // Individual button references
    private var volumeUpButton: ImageButton? = null
    private var volumeDownButton: ImageButton? = null
    private var muteButton: ImageButton? = null
    private var lockButton: ImageButton? = null

    // Layout parameters for positioning the overlay
    private var layoutParams: WindowManager.LayoutParams? = null

    // Vibrator for haptic feedback
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Variables for drag functionality
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isDragging: Boolean = false
    private val dragThreshold = 10 // Pixels moved before considering it a drag

    /**
     * Add the floating view to the screen
     * This makes the buttons visible
     */
    @SuppressLint("ClickableViewAccessibility")
    fun addToWindow() {
        // Inflate the layout from XML
        floatingView = LayoutInflater.from(context)
            .inflate(R.layout.floating_button_layout, null)

        // Set up window layout parameters
        layoutParams = createLayoutParams()

        // Find all buttons in the layout
        initializeButtons()

        // Set up click listeners for all buttons
        setupButtonListeners()

        // Set up drag functionality
        setupDragListener()

        // Add the view to window manager (makes it visible)
        try {
            windowManager.addView(floatingView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create WindowManager.LayoutParams
     * These parameters define how the overlay behaves and appears
     */
    private fun createLayoutParams(): WindowManager.LayoutParams {
        // Determine the overlay type based on Android version
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ requires TYPE_APPLICATION_OVERLAY
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // Older versions use TYPE_PHONE
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            // WRAP_CONTENT means the window size matches the content size
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,

            // Window type - determines how it interacts with other windows
            overlayType,

            // Flags - control window behavior
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // Don't take keyboard focus
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or  // Allow touches outside
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,  // Detect outside touches

            // Pixel format - how the window is drawn
            PixelFormat.TRANSLUCENT
        ).apply {
            // Position the overlay at the right center of the screen initially
            gravity = Gravity.TOP or Gravity.START
            x = 100  // 100 pixels from left
            y = 500  // 500 pixels from top
        }
    }

    /**
     * Initialize all button references
     * Connect the buttons to their XML counterparts
     */
    private fun initializeButtons() {
        floatingView?.let { view ->
            volumeUpButton = view.findViewById(R.id.volumeUpButton)
            volumeDownButton = view.findViewById(R.id.volumeDownButton)
            muteButton = view.findViewById(R.id.muteButton)
            lockButton = view.findViewById(R.id.lockButton)
        }
    }

    /**
     * Set up click listeners for all buttons
     * Define what happens when each button is tapped
     */
    private fun setupButtonListeners() {
        // Volume Up Button
        volumeUpButton?.setOnClickListener {
            if (!isDragging) {
                performHapticFeedback()
                volumeController.increaseVolume()
            }
        }

        // Volume Down Button
        volumeDownButton?.setOnClickListener {
            if (!isDragging) {
                performHapticFeedback()
                volumeController.decreaseVolume()
            }
        }

        // Mute Button
        muteButton?.setOnClickListener {
            if (!isDragging) {
                performHapticFeedback()
                volumeController.toggleMute()
            }
        }

        // Lock Screen Button
        lockButton?.setOnClickListener {
            if (!isDragging) {
                performHapticFeedback()
                screenLockHelper.lockScreen()
            }
        }
    }

    /**
     * Set up drag functionality
     * Allows user to move the floating buttons around the screen
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragListener() {
        floatingView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // User touched the view
                    // Remember initial positions
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // User is moving their finger
                    // Calculate how far they've moved
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY

                    // If moved more than threshold, it's a drag
                    if (abs(deltaX) > dragThreshold || abs(deltaY) > dragThreshold) {
                        isDragging = true

                        // Update position
                        layoutParams?.let { params ->
                            params.x = initialX + deltaX.toInt()
                            params.y = initialY + deltaY.toInt()

                            // Update the window position
                            try {
                                windowManager.updateViewLayout(floatingView, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // User released their finger
                    // If it was a drag, don't trigger button clicks
                    if (isDragging) {
                        // Reset drag state after a short delay
                        view.postDelayed({ isDragging = false }, 100)
                    }
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Perform haptic feedback (vibration)
     * Gives tactile feedback when user presses a button
     */
    private fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ - Use VibrationEffect
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,  // Duration in milliseconds
                        VibrationEffect.DEFAULT_AMPLITUDE  // Vibration strength
                    )
                )
            } else {
                // Older Android versions
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // Vibration might fail on some devices, ignore
            e.printStackTrace()
        }
    }

    /**
     * Remove the floating view from screen
     * Called when service is stopped
     */
    fun removeFromWindow() {
        try {
            floatingView?.let { view ->
                if (view.windowToken != null && view.parent != null) {
                    windowManager.removeView(view)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            floatingView = null
        }
    }
}