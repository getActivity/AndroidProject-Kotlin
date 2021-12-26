package com.hjq.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.hjq.widget.R
import kotlin.math.max
import kotlin.math.min

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/07/11
 *    desc   : 自定义评分控件（系统的 RatingBar 不好用）
 */
class SimpleRatingBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    /** 默认的星星图标 */
    private var normalDrawable: Drawable

    /** 选中的星星图标 */
    private var fillDrawable: Drawable

    /** 选中的半星图标 */
    private var halfDrawable: Drawable?

    /** 当前星等级 */
    private var currentGrade: Float

    /** 星星总数量 */
    private var gradeCount: Int

    /** 星星的宽度 */
    private var gradeWidth: Int

    /** 星星的高度 */
    private var gradeHeight: Int

    /** 星星之间的间隔 */
    private var gradeSpace: Int

    /** 星星选择跨度 */
    private var gradeStep: GradleStep? = null

    /** 星星变化监听事件 */
    private var listener: OnRatingChangeListener? = null

    /** 星星位置记录 */
    private val gradeBounds: Rect = Rect()

    init {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar)
        normalDrawable = ContextCompat.getDrawable(getContext(), array.getResourceId(
            R.styleable.SimpleRatingBar_normalDrawable, R.drawable.rating_star_off_ic))!!
        halfDrawable = ContextCompat.getDrawable(getContext(), array.getResourceId(
            R.styleable.SimpleRatingBar_halfDrawable, R.drawable.rating_star_half_ic))
        fillDrawable = ContextCompat.getDrawable(getContext(), array.getResourceId(
            R.styleable.SimpleRatingBar_fillDrawable, R.drawable.rating_star_fill_ic))!!

        // 两张图片的宽高不一致
        if ((normalDrawable.intrinsicWidth != fillDrawable.intrinsicWidth) || (
                    normalDrawable.intrinsicWidth != halfDrawable?.intrinsicWidth) || (
                    normalDrawable.intrinsicHeight != fillDrawable.intrinsicHeight) || (
                    normalDrawable.intrinsicHeight != halfDrawable?.intrinsicHeight)) {
            throw IllegalStateException("The width and height of the picture do not agree")
        }
        currentGrade = array.getFloat(R.styleable.SimpleRatingBar_grade, 0f)
        gradeCount = array.getInt(R.styleable.SimpleRatingBar_gradeCount, 5)
        gradeWidth = array.getDimensionPixelSize(R.styleable.SimpleRatingBar_gradeWidth, normalDrawable.intrinsicWidth)
        gradeHeight = array.getDimensionPixelSize(R.styleable.SimpleRatingBar_gradeHeight, fillDrawable.intrinsicHeight)
        gradeSpace = array.getDimension(R.styleable.SimpleRatingBar_gradeSpace, gradeWidth / 4f).toInt()
        gradeStep = when (array.getInt(R.styleable.SimpleRatingBar_gradeStep, 0)) {
            0x01 -> GradleStep.ONE
            0x00 -> GradleStep.HALF
            else -> GradleStep.HALF
        }
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth: Int = (gradeWidth * gradeCount) + (gradeSpace * (gradeCount + 1))
        val measuredHeight: Int = gradeHeight
        setMeasuredDimension(measuredWidth + paddingLeft + paddingRight,
            measuredHeight + paddingTop + paddingBottom)
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 如果控件处于不可用状态，直接不处理
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                var grade = 0f
                val distance: Float = event.x - paddingLeft - gradeSpace
                if (distance > 0) {
                    grade = distance / (gradeWidth + gradeSpace)
                }

                grade = min(max(grade, 0f), gradeCount.toFloat())

                if (grade - grade.toInt() > 0) {
                    grade = if (grade - grade.toInt() > 0.5f) {
                        // 0.5 - 1 算一颗星
                        (grade + 0.5f).toInt().toFloat()
                    } else {
                        // 0 - 0.5 算半颗星
                        grade.toInt() + 0.5f
                    }
                }
                if (grade * 10 != currentGrade * 10) {
                    currentGrade = grade
                    invalidate()
                    listener?.onRatingChanged(this, currentGrade, true)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 0 until gradeCount) {
            val start: Int = gradeSpace + (gradeWidth + gradeSpace) * i
            gradeBounds.left = paddingLeft + start
            gradeBounds.top = paddingTop
            gradeBounds.right = gradeBounds.left + gradeWidth
            gradeBounds.bottom = gradeBounds.top + gradeHeight
            if (currentGrade > i) {
                if ((halfDrawable != null) && (gradeStep == GradleStep.HALF) && (
                            currentGrade.toInt() == i) && (currentGrade - currentGrade.toInt() == 0.5f)) {
                    halfDrawable!!.bounds = gradeBounds
                    halfDrawable!!.draw(canvas)
                } else {
                    fillDrawable.bounds = gradeBounds
                    fillDrawable.draw(canvas)
                }
            } else {
                normalDrawable.bounds = gradeBounds
                normalDrawable.draw(canvas)
            }
        }
    }

    fun setRatingDrawable(@DrawableRes normalDrawableId: Int, @DrawableRes halfDrawableId: Int, @DrawableRes fillDrawableId: Int) {
        setRatingDrawable(ContextCompat.getDrawable(context, normalDrawableId)!!,
            ContextCompat.getDrawable(context, halfDrawableId),
            ContextCompat.getDrawable(context, fillDrawableId)!!)
    }

    fun setRatingDrawable(normalDrawable: Drawable, halfDrawable: Drawable?, fillDrawable: Drawable) {

        // 两张图片的宽高不一致
        if (normalDrawable.intrinsicWidth != fillDrawable.intrinsicWidth ||
            normalDrawable.intrinsicHeight != fillDrawable.intrinsicHeight) {
            throw IllegalStateException("The width and height of the picture do not agree")
        }
        this.normalDrawable = normalDrawable
        this.halfDrawable = halfDrawable
        this.fillDrawable = fillDrawable
        gradeWidth = this.normalDrawable.intrinsicWidth
        gradeHeight = this.normalDrawable.intrinsicHeight
        requestLayout()
    }

    fun getGrade(): Float {
        return currentGrade
    }

    fun setGrade(grade: Float) {
        var finalGrade: Float = grade
        if (finalGrade > gradeCount) {
            finalGrade = gradeCount.toFloat()
        }
        if (finalGrade - finalGrade.toInt() != 0.5f || finalGrade - finalGrade.toInt() > 0) {
            throw IllegalArgumentException("grade must be a multiple of 0.5f")
        }
        currentGrade = finalGrade
        invalidate()
        listener?.onRatingChanged(this, currentGrade, false)
    }

    fun getGradeCount(): Int {
        return gradeCount
    }

    fun setGradeCount(count: Int) {
        if (count > currentGrade) {
            currentGrade = count.toFloat()
        }
        gradeCount = count
        invalidate()
    }

    fun setGradeSpace(space: Int) {
        gradeSpace = space
        requestLayout()
    }

    fun setGradeStep(step: GradleStep?) {
        gradeStep = step
        invalidate()
    }

    fun getGradeStep(): GradleStep? {
        return gradeStep
    }

    fun setOnRatingBarChangeListener(listener: OnRatingChangeListener?) {
        this.listener = listener
    }

    enum class GradleStep {

        /** 半颗星 */
        HALF,

        /** 一颗星 */
        ONE
    }

    interface OnRatingChangeListener {

        /**
         * 评分发生变化监听时回调
         *
         * @param grade             当前星星数
         * @param touch             是否通过触摸改变
         */
        fun onRatingChanged(ratingBar: SimpleRatingBar, grade: Float, touch: Boolean)
    }
}