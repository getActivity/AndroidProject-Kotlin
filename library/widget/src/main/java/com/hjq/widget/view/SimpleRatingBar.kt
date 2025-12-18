package com.hjq.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.hjq.widget.R
import kotlin.math.roundToInt

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/07/11
 *    desc   : 自定义评分控件（系统的 RatingBar 不好用）
 */
class SimpleRatingBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    /** 默认的星星图标  */
    private lateinit var normalDrawable: Drawable

    /** 选中的星星图标  */
    private lateinit var fillDrawable: Drawable

    /** 选中的半星图标  */
    private lateinit var halfDrawable: Drawable

    /** 当前星等级  */
    private var currentGrade = 0f

    /** 星星总数量  */
    private var gradeCount = 0

    /** 星星的宽度  */
    private var gradeWidth = 0

    /** 星星的高度  */
    private var gradeHeight = 0

    /** 星星之间的间隔  */
    private var gradeSpace = 0

    /** 星星选择跨度  */
    private var gradeStep: GradleStep? = null

    /** 星星变化监听事件  */
    private var listener: OnRatingChangeListener? = null

    /** 星星位置记录  */
    private val gradeBounds = Rect()

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SimpleRatingBar)
        setRatingDrawable(requireNotNull(ContextCompat.getDrawable(getContext(), array.getResourceId(R.styleable.SimpleRatingBar_normalDrawable, R.drawable.rating_star_off_ic))),
                            ContextCompat.getDrawable(getContext(), array.getResourceId(R.styleable.SimpleRatingBar_halfDrawable, R.drawable.rating_star_half_ic)),
                             requireNotNull(ContextCompat.getDrawable(getContext(), array.getResourceId(R.styleable.SimpleRatingBar_fillDrawable, R.drawable.rating_star_fill_ic))))
        setGradeCount(array.getInt(R.styleable.SimpleRatingBar_gradeCount, 5))
        setGradeSpace(array.getDimension(R.styleable.SimpleRatingBar_gradeSpace, gradeWidth / 4f).toInt())
        setGradeWidth(array.getDimensionPixelSize(R.styleable.SimpleRatingBar_gradeWidth, requireNotNull(normalDrawable).intrinsicWidth))
        setGradeHeight(array.getDimensionPixelSize(R.styleable.SimpleRatingBar_gradeHeight, requireNotNull(normalDrawable).intrinsicHeight))
        when (array.getInt(R.styleable.SimpleRatingBar_gradeStep, 0)) {
            0x01 -> setGradeStep(GradleStep.ONE)
            0x00 -> setGradeStep(GradleStep.HALF)
            else -> setGradeStep(GradleStep.HALF)
        }
        setGrade(array.getFloat(R.styleable.SimpleRatingBar_grade, 0f))
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = gradeWidth * gradeCount + gradeSpace * (gradeCount + 1)
        val measuredHeight = gradeHeight
        setMeasuredDimension(measuredWidth + paddingLeft + paddingRight,
            measuredHeight + paddingTop + paddingBottom)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 如果控件处于不可用状态，直接不处理
        if (!isEnabled) {
            return false
        }
        val layoutDirection = resources.configuration.layoutDirection
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                var grade = 0f
                val distance: Float
                if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                    distance = (width - event.x) - getPaddingRight() - gradeSpace
                } else {
                    distance = event.x - getPaddingLeft() - gradeSpace
                }
                if (distance > 0) {
                    grade = distance / (gradeWidth + gradeSpace)
                }
                grade = Math.min(Math.max(grade, 0f), gradeCount.toFloat())
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
                    optimizationGradeValue()
                    invalidate()
                    listener?.onRatingChanged(this, currentGrade, true)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 0 until gradeCount) {
            val layoutDirection = resources.configuration.layoutDirection
            val visualIndex = if (layoutDirection == LAYOUT_DIRECTION_RTL) (gradeCount - 1 - i) else i
            val start = gradeSpace + (gradeWidth + gradeSpace) * visualIndex

            gradeBounds.left = paddingLeft + start
            gradeBounds.top = paddingTop
            gradeBounds.right = gradeBounds.left + gradeWidth
            gradeBounds.bottom = gradeBounds.top + gradeHeight

            if (currentGrade > i) {
                if (gradeStep == GradleStep.HALF && currentGrade.toInt() == i && currentGrade - currentGrade.toInt() == 0.5f) {
                    drawDrawableRtlAware(canvas, halfDrawable, gradeBounds)
                } else {
                    drawDrawableRtlAware(canvas, fillDrawable, gradeBounds)
                }
            } else {
                drawDrawableRtlAware(canvas, normalDrawable, gradeBounds)
            }
        }
    }

    private fun drawDrawableRtlAware(canvas: Canvas, drawable: Drawable, bounds: Rect) {
        drawable.bounds = bounds
        if (layoutDirection == LAYOUT_DIRECTION_RTL) {
            val cx = bounds.exactCenterX()
            val cy = bounds.exactCenterY()
            canvas.save()
            canvas.scale(-1f, 1f, cx, cy)
            drawable.draw(canvas)
            canvas.restore()
        } else {
            drawable.draw(canvas)
        }
    }

    fun setRatingDrawable(normalDrawable: Drawable, halfDrawable: Drawable? = null, fillDrawable: Drawable) {
        this.normalDrawable = normalDrawable
        // 如果 halfDrawable 是 null，就用 normalDrawable 代替
        this.halfDrawable = halfDrawable ?: normalDrawable
        this.fillDrawable = fillDrawable

        // 两张图片的宽高不一致
        check(!(this.normalDrawable.intrinsicWidth != this.fillDrawable.intrinsicWidth ||
                      this.normalDrawable.intrinsicHeight != this.fillDrawable.intrinsicHeight)
        ) { "The width and height of the picture do not agree" }

        check(!(this.normalDrawable.intrinsicWidth != this.halfDrawable.intrinsicWidth ||
                       this.normalDrawable.intrinsicHeight != this.halfDrawable.intrinsicHeight)
        ) { "The width and height of the picture do not agree" }

        if (gradeWidth == this.normalDrawable.intrinsicWidth) {
            gradeWidth = 0
        }
        if (gradeHeight == this.normalDrawable.intrinsicHeight) {
            gradeHeight = 0
        }

        if (gradeWidth == 0) {
            gradeWidth = this.normalDrawable.intrinsicWidth
        }
        if (gradeHeight == 0) {
            gradeHeight = this.normalDrawable.intrinsicHeight
        }
        requestLayout()
    }

    fun getGrade(): Float {
        return currentGrade
    }

    fun setGrade(grade: Float) {
        currentGrade = grade
        if (currentGrade > gradeCount) {
            currentGrade = gradeCount.toFloat()
        }
        optimizationGradeValue()
        invalidate()
        listener?.onRatingChanged(this, currentGrade, false)
    }

    fun getGradeCount(): Int {
        return gradeCount
    }

    fun setGradeCount(count: Int) {
        require(count > 0) { "grade count cannot be less than or equal to 0" }
        if (count > currentGrade) {
            currentGrade = count.toFloat()
        }
        gradeCount = count
        invalidate()
    }

    fun getGradeWidth(): Int {
        return gradeWidth
    }

    fun setGradeWidth(width: Int) {
        gradeWidth = width
        requestLayout()
    }

    fun getGradeHeight(): Int {
        return gradeHeight
    }

    fun setGradeHeight(height: Int) {
        gradeHeight = height
        requestLayout()
    }

    fun setGradeSpace(space: Int) {
        gradeSpace = space
        requestLayout()
    }

    fun setGradeStep(step: GradleStep) {
        gradeStep = step
        optimizationGradeValue()
        invalidate()
    }

    fun getGradeStep(): GradleStep? {
        return gradeStep
    }

    fun setOnRatingBarChangeListener(listener: OnRatingChangeListener?) {
        this.listener = listener
    }

    private fun optimizationGradeValue() {
        if ((currentGrade - currentGrade.toInt().toFloat()) == 0f) {
            return
        }
        when (gradeStep) {
            GradleStep.HALF -> {
                if (currentGrade - currentGrade.toInt().toFloat() > 0.5f) {
                    currentGrade = currentGrade.roundToInt().toFloat()
                } else if (currentGrade - currentGrade.toInt().toFloat() != 0.5f) {
                    currentGrade += 0.5f
                }
            }
            GradleStep.ONE -> {
                currentGrade = currentGrade.roundToInt().toFloat()
            }
            else -> {
                currentGrade = currentGrade.roundToInt().toFloat()
            }
        }
    }

    enum class GradleStep {

        /** 半颗星  */
        HALF,

        /** 一颗星  */
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