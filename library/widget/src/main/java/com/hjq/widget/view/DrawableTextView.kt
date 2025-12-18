package com.hjq.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.hjq.widget.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/04/18
 *    desc   : 支持限定 Drawable 大小的 TextView
 */
class DrawableTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    private val DRAWABLE_INDEX_LEFT = 0
    private val DRAWABLE_INDEX_TOP = 1
    private val DRAWABLE_INDEX_RIGHT = 2
    private val DRAWABLE_INDEX_BOTTOM = 3

    private var drawableWidth: Int
    private var drawableHeight: Int

    init {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView)
        drawableWidth = array.getDimensionPixelSize(R.styleable.DrawableTextView_drawableWidth, 0)
        drawableHeight = array.getDimensionPixelSize(R.styleable.DrawableTextView_drawableHeight, 0)
        array.recycle()
        refreshDrawablesSize()
    }

    /**
     * 限定 Drawable 大小
     */
    fun setDrawableSize(width: Int, height: Int) {
        drawableWidth = width
        drawableHeight = height
        refreshDrawablesSize()
    }

    /**
     * 限定 Drawable 宽度
     */
    fun setDrawableWidth(width: Int) {
        drawableWidth = width
        refreshDrawablesSize()
    }

    /**
     * 限定 Drawable 高度
     */
    fun setDrawableHeight(height: Int) {
        drawableHeight = height
        refreshDrawablesSize()
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawables(left, top, right, bottom)
        refreshDrawablesSize()
    }

    override fun setCompoundDrawablesRelative(start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawablesRelative(start, top, end, bottom)
        refreshDrawablesSize()
    }

    /**
     * 刷新 Drawable 列表大小
     */
    private fun refreshDrawablesSize() {
        if (drawableWidth == 0 || drawableHeight == 0) {
            return
        }
        val compoundDrawables = compoundDrawables
        val compoundDrawablesRelative = compoundDrawablesRelative

        // 适配 RTL 特性
        val layoutDirection = resources.configuration.layoutDirection

        var leftDrawable = compoundDrawablesRelative[if (layoutDirection == LAYOUT_DIRECTION_LTR) DRAWABLE_INDEX_LEFT else DRAWABLE_INDEX_RIGHT]
        if (leftDrawable == null) {
            leftDrawable = compoundDrawables[DRAWABLE_INDEX_LEFT]
        }

        var topDrawable = compoundDrawablesRelative[DRAWABLE_INDEX_TOP]
        if (topDrawable == null) {
            topDrawable = compoundDrawables[DRAWABLE_INDEX_TOP]
        }

        var rightDrawable = compoundDrawablesRelative[if (layoutDirection == LAYOUT_DIRECTION_LTR) DRAWABLE_INDEX_RIGHT else DRAWABLE_INDEX_LEFT]
        if (rightDrawable == null) {
            rightDrawable = compoundDrawables[DRAWABLE_INDEX_RIGHT]
        }

        var bottomDrawable = compoundDrawablesRelative[DRAWABLE_INDEX_BOTTOM]
        if (bottomDrawable == null) {
            bottomDrawable = compoundDrawables[DRAWABLE_INDEX_BOTTOM]
        }

        val newDrawable = arrayOfNulls<Drawable>(4)
        newDrawable[DRAWABLE_INDEX_LEFT] = limitDrawableSize(leftDrawable)
        newDrawable[DRAWABLE_INDEX_TOP] = limitDrawableSize(topDrawable)
        newDrawable[DRAWABLE_INDEX_RIGHT] = limitDrawableSize(rightDrawable)
        newDrawable[DRAWABLE_INDEX_BOTTOM] = limitDrawableSize(bottomDrawable)

        super.setCompoundDrawables(
            newDrawable[DRAWABLE_INDEX_LEFT], newDrawable[DRAWABLE_INDEX_TOP],
            newDrawable[DRAWABLE_INDEX_RIGHT], newDrawable[DRAWABLE_INDEX_BOTTOM])
    }

    /**
     * 重新限定 Drawable 宽高
     */
    private fun limitDrawableSize(drawable: Drawable?): Drawable? {
        if (drawable == null) {
            return null
        }
        if (drawableWidth == 0 || drawableHeight == 0) {
            return drawable
        }
        drawable.setBounds(0, 0, drawableWidth, drawableHeight)
        return drawable
    }
}