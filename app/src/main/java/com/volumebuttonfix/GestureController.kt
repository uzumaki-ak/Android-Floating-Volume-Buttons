package com.volumebuttonfix

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

/**
 * GestureController - Handles swipe gestures on floating button
 *
 * Detects:
 * - Swipe up → Volume up
 * - Swipe down → Volume down
 * - Double tap → Toggle collapsed/expanded
 *
 * Path: app/src/main/java/com/volumebuttonfix/GestureController.kt
 * Action: CREATE NEW FILE
 */
class GestureController(
    context: Context,
    private val onSwipeUp: () -> Unit,
    private val onSwipeDown: () -> Unit,
    private val onDoubleTap: () -> Unit
) {

    private val gestureDetector: GestureDetector

    companion object {
        private const val SWIPE_THRESHOLD = 100 // Minimum distance for swipe
        private const val SWIPE_VELOCITY_THRESHOLD = 100 // Minimum velocity
    }

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            /**
             * Handle double tap
             */
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap.invoke()
                return true
            }

            /**
             * Handle fling (swipe) gestures
             */
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                // Check if it's primarily a vertical swipe
                if (abs(diffY) > abs(diffX)) {
                    // Check if swipe distance and velocity are sufficient
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            // Swipe down
                            onSwipeDown.invoke()
                        } else {
                            // Swipe up
                            onSwipeUp.invoke()
                        }
                        return true
                    }
                }
                return false
            }

            /**
             * Single tap confirmation
             */
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Not used, but could trigger expand/collapse
                return super.onSingleTapConfirmed(e)
            }
        })
    }

    /**
     * Process touch event
     * Call this from onTouchListener
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}