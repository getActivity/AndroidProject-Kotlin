package com.hjq.demo.other

import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.hjq.demo.R
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.simple.SimpleComponent
import kotlin.math.min

/**
 *    author : 树朾 & Android 轮子哥
 *    github : https://github.com/scwang90/SmartRefreshLayout/tree/master/refresh-footer-ball
 *    time   : 2020/08/01
 *    desc   : 球脉冲底部加载组件
 */
class SmartBallPulseFooter @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SimpleComponent(context, attrs, 0), RefreshFooter {

    private val interpolator: TimeInterpolator = AccelerateDecelerateInterpolator()

    private var noMoreData: Boolean = false
    private var manualNormalColor: Boolean = false
    private var manualAnimationColor: Boolean = false
    private val paint: Paint = Paint()
    private var normalColor: Int = Color.parseColor("#EEEEEE")

    private var animatingColor: IntArray = intArrayOf(
        Color.parseColor("#30B399"),
        Color.parseColor("#FF4600"),
        Color.parseColor("#142DCC"))

    private val circleSpacing: Float
    private var startTime: Long = 0
    private var started: Boolean = false
    private val textWidth: Float

    init {
        minimumHeight = resources.getDimension(R.dimen.dp_60).toInt()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        mSpinnerStyle = SpinnerStyle.Translate
        circleSpacing = resources.getDimension(R.dimen.dp_2)
        paint.textSize = resources.getDimension(R.dimen.sp_14)
        textWidth = paint.measureText(getContext().getString(R.string.common_no_more_data))
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width: Int = width
        val height: Int = height
        if (noMoreData) {
            paint.color = Color.parseColor("#898989")
            canvas.drawText(context.getString(R.string.common_no_more_data),
                (width - textWidth) / 2, (height - paint.textSize) / 2, paint)
        } else {
            val radius: Float = (min(width, height) - circleSpacing * 2) / 7
            val x: Float = width / 2f - (radius * 2 + circleSpacing)
            val y: Float = height / 2f
            val now: Long = System.currentTimeMillis()
            for (i in 0..2) {
                val time: Long = now - startTime - (120 * (i + 1))
                var percent: Float = if (time > 0) ((time % 750) / 750f) else 0f
                percent = interpolator.getInterpolation(percent)
                canvas.save()
                val translateX: Float = x + ((radius * 2) * i) + (circleSpacing * i)
                if (percent < 0.5) {
                    val scale: Float = 1 - percent * 2 * 0.7f
                    val translateY: Float = y - scale * 10
                    canvas.translate(translateX, translateY)
                } else {
                    val scale: Float = percent * 2 * 0.7f - 0.4f
                    val translateY: Float = y + scale * 10
                    canvas.translate(translateX, translateY)
                }
                paint.color = animatingColor[i % animatingColor.size]
                canvas.drawCircle(0f, 0f, radius / 3, paint)
                canvas.restore()
            }
        }
        if (started) {
            postInvalidate()
        }
    }

    override fun onStartAnimator(layout: RefreshLayout, height: Int, maxDragHeight: Int) {
        if (started) {
            return
        }
        invalidate()
        started = true
        startTime = System.currentTimeMillis()
    }

    override fun onFinish(layout: RefreshLayout, success: Boolean): Int {
        started = false
        startTime = 0
        paint.color = normalColor
        return 0
    }

    override fun setPrimaryColors(@ColorInt vararg colors: Int) {
        if (!manualAnimationColor && colors.size > 1) {
            setAnimatingColor(colors[0])
            manualAnimationColor = false
        }
        if (!manualNormalColor) {
            if (colors.size > 1) {
                setNormalColor(colors[1])
            } else if (colors.isNotEmpty()) {
                setNormalColor(ColorUtils.compositeColors(Color.parseColor("#99FFFFFF"), colors[0]))
            }
            manualNormalColor = false
        }
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        this.noMoreData = noMoreData
        return true
    }

    fun setSpinnerStyle(style: SpinnerStyle?): SmartBallPulseFooter = apply {
        mSpinnerStyle = style
    }

    fun setNormalColor(@ColorInt color: Int): SmartBallPulseFooter = apply {
        normalColor = color
        manualNormalColor = true
        if (!started) {
            paint.color = color
        }
    }

    fun setAnimatingColor(@ColorInt color: Int): SmartBallPulseFooter = apply {
        animatingColor = intArrayOf(color)
        manualAnimationColor = true
        if (started) {
            paint.color = color
        }
    }
}