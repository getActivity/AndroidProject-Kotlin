package com.hjq.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import kotlin.math.max

/**
 *    author : tobiasrohloff & Android 轮子哥
 *    github : https://github.com/tobiasrohloff/NestedScrollWebView
 *    time   : 2020/08/18
 *    desc   : 支持嵌套滚动的 WebView
 */
open class NestedScrollWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)  :
    WebView(context, attrs, defStyleAttr, defStyleRes), NestedScrollingChild {

    @Suppress("LeakingThis")
    private val childHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    private var lastMotionY: Int = 0
    private val scrollOffset: IntArray = IntArray(2)
    private val scrollConsumed: IntArray = IntArray(2)
    private var nestedOffsetY: Int = 0
    private var change: Boolean = false

    init {
        isNestedScrollingEnabled = true
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = false
        val trackedEvent: MotionEvent = MotionEvent.obtain(event)
        val action: Int = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0
        }
        val y: Int = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY.toFloat())
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                change = false
                lastMotionY = y
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                result = super.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaY: Int = lastMotionY - y
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    trackedEvent.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                }
                lastMotionY = y - scrollOffset[1]
                val oldY: Int = scrollY
                val newScrollY: Int = max(0, oldY + deltaY)
                val dyConsumed: Int = newScrollY - oldY
                val dyUnconsumed: Int = deltaY - dyConsumed
                if (dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, scrollOffset)) {
                    lastMotionY -= scrollOffset[1]
                    trackedEvent.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                }
                if (scrollConsumed[1] == 0 && scrollOffset[1] == 0) {
                    if (change) {
                        change = false
                        trackedEvent.action = MotionEvent.ACTION_DOWN
                        super.onTouchEvent(trackedEvent)
                    } else {
                        result = super.onTouchEvent(trackedEvent)
                    }
                    trackedEvent.recycle()
                } else {
                    if (!change) {
                        change = true
                        super.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll()
                result = super.onTouchEvent(event)
            }
        }
        return result
    }

    /**
     * [NestedScrollingChild]
     */
    final override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }
}