package com.hjq.custom.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.hjq.custom.widget.R
import kotlin.math.max

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 简单的 Layout 容器，比 FrameLayout 更加轻量
 *             可以用于自定义组合控件继承的基类，可以起到性能优化的作用
 *             另外还支持限制最大宽高和最小宽高
 */
open class SimpleLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    /** 布局最大宽度  */
    private var maxWidth: Int = 0

    /** 布局最大高度  */
    private var maxHeight: Int = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SimpleLayout)
        maxWidth = array.getDimensionPixelSize(R.styleable.SimpleLayout_android_maxWidth, 0)
        maxHeight = array.getDimensionPixelSize(R.styleable.SimpleLayout_android_maxHeight, 0)
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        var viewMaxHeight = 0
        var viewMaxWidth = 0
        var childState = 0

        for (i in 0 until count) {

            val childView = getChildAt(i)
            if (childView.visibility == GONE) {
                // 不测量隐藏的控件，因为没有任何意义
                continue
            }

            measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val measuredWidth = childView.measuredWidth
            val measuredHeight = childView.measuredHeight
            if (maxWidth in 1 until measuredWidth ||
                maxHeight in 1 until measuredHeight) {
                var childWidthMeasureSpec = widthMeasureSpec
                var childHeightMeasureSpec = heightMeasureSpec
                if (maxWidth in 1 until measuredWidth) {
                    childWidthMeasureSpec =
                        MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY)
                }
                if (maxHeight in 1 until measuredHeight) {
                    childHeightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY)
                }
                // 如果测量出来的控件大小已经超过了布局自身的大小，那么就进行二次测量
                measureChildWithMargins(childView, childWidthMeasureSpec, 0, childHeightMeasureSpec, 0)
            }

            val params = childView.layoutParams as MarginLayoutParams
            viewMaxWidth = max(viewMaxWidth, childView.measuredWidth + params.leftMargin + params.rightMargin)
            viewMaxHeight = max(viewMaxHeight, childView.measuredHeight + params.topMargin + params.bottomMargin)
            childState = combineMeasuredStates(childState, childView.measuredState)
        }

        viewMaxWidth += paddingLeft + paddingRight
        viewMaxHeight += paddingTop + paddingBottom

        viewMaxWidth = max(viewMaxWidth, suggestedMinimumWidth)
        viewMaxHeight = max(viewMaxHeight, suggestedMinimumHeight)

        setMeasuredDimension(resolveSizeAndState(viewMaxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(viewMaxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 遍历子 View
        val count = childCount
        for (i in 0 until count) {
            val childView = getChildAt(i)
            val params = childView.layoutParams as MarginLayoutParams
            val left = params.leftMargin + paddingLeft
            val top = params.topMargin + paddingTop
            val right = left + childView.measuredWidth
            val bottom = top + childView.measuredHeight
            // 将子 View 放置到左上角的位置
            childView.layout(left, top, right, bottom)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams? {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(params: LayoutParams): LayoutParams? {
        return MarginLayoutParams(params)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun checkLayoutParams(params: LayoutParams?): Boolean {
        return params is MarginLayoutParams
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    open fun getMaxWidth(): Int {
        return maxWidth
    }

    open fun setMaxWidth(width: Int) {
        maxWidth = width
        requestLayout()
    }

    open fun getMaxHeight(): Int {
        return maxHeight
    }

    open fun setMaxHeight(height: Int) {
        maxHeight = height
        requestLayout()
    }
}