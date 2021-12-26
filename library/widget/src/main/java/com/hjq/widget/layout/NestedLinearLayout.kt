package com.hjq.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import androidx.core.view.*
import kotlin.math.abs

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/08/11
 *    desc   : 支持嵌套滚动的 LinearLayout
 */
class NestedLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes), NestedScrollingChild, NestedScrollingParent {

    companion object {
        private const val INVALID_POINTER: Int = -1
    }

    private val childHelper: NestedScrollingChildHelper
    private val parentHelper: NestedScrollingParentHelper
    private val scrollConsumed: IntArray = IntArray(2)
    private val scrollOffset: IntArray = IntArray(2)
    private val touchSlop: Float
    private val maximumVelocity: Float
    private val minimumVelocity: Float
    private var lastMotionY: Int = 0
    private var activePointerId: Int = 0
    private var velocityTracker: VelocityTracker? = null
    private var beingDragged: Boolean = false

    init {
        setWillNotDraw(false)
        childHelper = NestedScrollingChildHelper(this)
        parentHelper = NestedScrollingParentHelper(this)
        isNestedScrollingEnabled = true
        touchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop.toFloat()
        maximumVelocity = ViewConfiguration.get(getContext()).scaledMaximumFlingVelocity.toFloat()
        minimumVelocity = ViewConfiguration.get(getContext()).scaledMinimumFlingVelocity.toFloat()
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val event: MotionEvent = MotionEvent.obtain(ev)
        initVelocityTrackerIfNotExists()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.addMovement(ev)
                lastMotionY = event.y.toInt()
                activePointerId = event.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE -> run {
                val activePointerIndex: Int = event.findPointerIndex(activePointerId)
                if (activePointerIndex == -1) {
                    return@run
                }
                val y: Int = event.getY(activePointerIndex).toInt()
                var deltaY: Int = lastMotionY - y
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                }
                if (!beingDragged && abs(lastMotionY - y) > touchSlop) {
                    this.parent?.requestDisallowInterceptTouchEvent(true)
                    beingDragged = true
                    if (deltaY > 0) {
                        deltaY -= touchSlop.toInt()
                    } else {
                        deltaY += touchSlop.toInt()
                    }
                }
                if (beingDragged) {
                    velocityTracker?.addMovement(ev)
                    lastMotionY = y - scrollOffset[1]
                    if (dispatchNestedScroll(0, 0, 0, deltaY, scrollOffset)) {
                        lastMotionY -= scrollOffset[1]
                        event.offsetLocation(0f, scrollOffset[1].toFloat())
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (beingDragged) {
                    velocityTracker?.computeCurrentVelocity(1000, maximumVelocity)
                    var initialVelocity: Int? =
                        velocityTracker?.getYVelocity(activePointerId)?.toInt()
                    if (initialVelocity == null) {
                        initialVelocity = 0
                    }
                    if (abs(initialVelocity) > minimumVelocity) {
                        flingWithNestedDispatch(-initialVelocity)
                    }
                }
                activePointerId = INVALID_POINTER
                endDrag()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index: Int = event.actionIndex
                lastMotionY = event.getY(index).toInt()
                activePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                lastMotionY = event.getY(event.findPointerIndex(activePointerId)).toInt()
            }
        }
        event.recycle()
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex: Int = ((ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK)
                shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
        val pointerId: Int = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            val newPointerIndex: Int = if (pointerIndex == 0) 1 else 0
            lastMotionY = ev.getY(newPointerIndex).toInt()
            activePointerId = ev.getPointerId(newPointerIndex)
            velocityTracker?.clear()
        }
    }

    private fun endDrag() {
        beingDragged = false
        recycleVelocityTracker()
        stopNestedScroll()
    }

    private fun flingWithNestedDispatch(velocityY: Int) {
        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
            dispatchNestedFling(0f, velocityY.toFloat(), true)
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            recycleVelocityTracker()
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    // NestedScrollingChild
    override fun setNestedScrollingEnabled(enabled: Boolean) {
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

    // NestedScrollingParent
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return (nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        parentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
    }

    override fun onStopNestedScroll(target: View) {
        parentHelper.onStopNestedScroll(target)
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        dispatchNestedPreScroll(dx, dy, consumed, null)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return parentHelper.nestedScrollAxes
    }
}