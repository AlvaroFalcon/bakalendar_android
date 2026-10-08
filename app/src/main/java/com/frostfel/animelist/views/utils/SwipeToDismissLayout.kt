package com.frostfel.animelist.views.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * Drag the content down to dismiss, like a bottom sheet. While dragging, the screen behind
 * shows through a fading scrim (the window must be translucent). Releasing past a quarter of
 * the height, or with a downward fling, slides the content out and calls [onDismiss].
 */
class SwipeToDismissLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onDismiss: () -> Unit = {}

    /** Return true when the touch is on content that can still scroll up (scroll wins). */
    var canScrollUp: (MotionEvent) -> Boolean = { false }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = MIN_FLING_DP_PER_SECOND * resources.displayMetrics.density
    private var downX = 0f
    private var downY = 0f
    private var dragging = false
    private var blockedByScroll = false
    private var velocityTracker: VelocityTracker? = null

    private val content get() = getChildAt(0)

    init {
        setScrim(0f)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.rawX
                downY = ev.rawY
                dragging = false
                blockedByScroll = canScrollUp(ev)
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain().apply { addMovement(ev) }
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(ev)
                val dy = ev.rawY - downY
                val dx = ev.rawX - downX
                if (!blockedByScroll && dy > touchSlop && dy > abs(dx)) {
                    dragging = true
                    downY = ev.rawY
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
        return dragging
    }

    // A drag gesture, not a click: back/predictive back remain the accessible way to close.
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val content = content ?: return false
        velocityTracker?.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) return true
                val offset = (event.rawY - downY).coerceAtLeast(0f)
                content.translationY = offset
                setScrim(offset / height.coerceAtLeast(1))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) settle(event.actionMasked == MotionEvent.ACTION_UP)
                dragging = false
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return true
    }

    private fun settle(released: Boolean) {
        val content = content ?: return
        val velocity = velocityTracker?.run {
            computeCurrentVelocity(1000)
            yVelocity
        } ?: 0f
        val dismiss = released && (content.translationY > height * DISMISS_FRACTION || velocity > minFlingVelocity)
        val target = if (dismiss) height.toFloat() else 0f
        content.animate()
            .translationY(target)
            .setDuration(ANIMATION_MS)
            .setUpdateListener { setScrim(content.translationY / height.coerceAtLeast(1)) }
            .withEndAction { if (dismiss) onDismiss() }
            .start()
    }

    /** Dims what is behind: dark at rest, clearer the further the content is dragged. */
    private fun setScrim(progress: Float) {
        val alpha = ((1f - progress.coerceIn(0f, 1f)) * MAX_SCRIM_ALPHA).toInt()
        setBackgroundColor(Color.argb(alpha, 0, 0, 0))
    }

    private companion object {
        const val DISMISS_FRACTION = 0.25f
        const val MIN_FLING_DP_PER_SECOND = 1000
        const val MAX_SCRIM_ALPHA = 160
        const val ANIMATION_MS = 220L
    }
}
