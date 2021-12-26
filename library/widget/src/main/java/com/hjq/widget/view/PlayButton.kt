package com.hjq.widget.view

import android.animation.ValueAnimator
import android.content.*
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnticipateInterpolator
import com.hjq.widget.R

/**
 *    author : codeestX & Android 轮子哥
 *    github : https://github.com/codeestX/ENViews
 *    time   : 2021/09/12
 *    desc   : 播放暂停动效的按钮
 */
class PlayButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    companion object {

        /** 播放状态 */
        const val STATE_PLAY: Int = 0

        /** 暂停状态 */
        const val STATE_PAUSE: Int = 1
    }

    /** 当前状态 */
    private var currentState: Int = STATE_PAUSE

    /** 动画时间 */
    private var animDuration: Int

    private val paint: Paint
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var circleRadius: Int = 0
    private var rectF: RectF? = null
    private var bgRectF: RectF? = null
    private var fraction: Float = 1f
    private val path: Path
    private val dstPath: Path
    private val pathMeasure: PathMeasure
    private var pathLength: Float = 0f

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayButton)
        val lineColor: Int = typedArray.getColor(R.styleable.PlayButton_pb_lineColor, Color.WHITE)
        val lineSize: Int = typedArray.getInteger(
            R.styleable.PlayButton_pb_lineSize,
            resources.getDimension(R.dimen.dp_4).toInt()
        )
        animDuration = typedArray.getInteger(R.styleable.PlayButton_pb_animDuration, 200)
        typedArray.recycle()

        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = lineColor
        paint.strokeWidth = lineSize.toFloat()
        paint.pathEffect = CornerPathEffect(1f)
        path = Path()
        dstPath = Path()
        pathMeasure = PathMeasure()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        viewWidth = width * 9 / 10
        viewHeight = height * 9 / 10
        circleRadius = width / resources.getDimension(R.dimen.dp_4).toInt()
        centerX = width / 2
        centerY = height / 2
        rectF = RectF(
            (centerX - circleRadius).toFloat(), centerY + 0.6f * circleRadius,
            (centerX + circleRadius).toFloat(), centerY + 2.6f * circleRadius
        )
        bgRectF = RectF(
            centerX - viewWidth / 2f, centerY - viewHeight / 2f,
            centerX + viewWidth / 2f, centerY + viewHeight / 2f
        )
        path.moveTo((centerX - circleRadius).toFloat(), centerY + 1.8f * circleRadius)
        path.lineTo((centerX - circleRadius).toFloat(), centerY - 1.8f * circleRadius)
        path.lineTo((centerX + circleRadius).toFloat(), centerY.toFloat())
        path.close()
        pathMeasure.setPath(path, false)
        pathLength = pathMeasure.length
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var finalWidthMeasureSpec: Int = widthMeasureSpec
        var finalHeightMeasureSpec: Int = heightMeasureSpec
        when (MeasureSpec.getMode(finalWidthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED ->
                finalWidthMeasureSpec = MeasureSpec.makeMeasureSpec(resources.getDimension(R.dimen.dp_60).toInt(), MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        when (MeasureSpec.getMode(finalHeightMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED ->
                finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(resources.getDimension(R.dimen.dp_60).toInt(), MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        setMeasuredDimension(finalWidthMeasureSpec, finalHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), viewWidth / 2f, paint)
        when {
            fraction < 0 -> {
                // 弹性部分
                canvas.drawLine((centerX + circleRadius).toFloat(),
                    centerY - 1.6f * circleRadius + 10 * circleRadius * fraction, (centerX + circleRadius).toFloat(),
                    centerY + (1.6f * circleRadius) + (10 * circleRadius * fraction), paint)
                canvas.drawLine((centerX - circleRadius).toFloat(), centerY - 1.6f * circleRadius, (
                            centerX - circleRadius).toFloat(), centerY + 1.6f * circleRadius, paint)
                canvas.drawArc(bgRectF!!, -105f, 360f, false, paint)
            }
            fraction <= 0.3 -> {
                // 右侧直线和下方曲线
                canvas.drawLine((centerX + circleRadius).toFloat(),
                    centerY - 1.6f * circleRadius + circleRadius * 3.2f / 0.3f * fraction, (centerX + circleRadius).toFloat(),
                    centerY + 1.6f * circleRadius, paint)
                canvas.drawLine((centerX - circleRadius).toFloat(), centerY - 1.6f * circleRadius, (
                            centerX - circleRadius).toFloat(), centerY + 1.6f * circleRadius, paint)
                if (fraction != 0f) {
                    canvas.drawArc(rectF!!, 0f, 180f / 0.3f * fraction, false, paint)
                }
                canvas.drawArc(bgRectF!!, -105 + 360 * fraction, 360 * (1 - fraction), false, paint)
            }
            fraction <= 0.6 -> {
                // 下方曲线和三角形
                canvas.drawArc(rectF!!, 180f / 0.3f * (fraction - 0.3f),
                    180 - 180f / 0.3f * (fraction - 0.3f), false, paint)
                dstPath.reset()
                pathMeasure.getSegment(0.02f * pathLength,
                    0.38f * pathLength + 0.42f * pathLength / 0.3f * (fraction - 0.3f),
                    dstPath, true)
                canvas.drawPath(dstPath, paint)
                canvas.drawArc(bgRectF!!, -105 + 360 * fraction, 360 * (1 - fraction), false, paint)
            }
            fraction <= 0.8 -> {
                // 三角形
                dstPath.reset()
                pathMeasure.getSegment(0.02f * pathLength + 0.2f * pathLength / 0.2f * (fraction - 0.6f),
                    0.8f * pathLength + 0.2f * pathLength / 0.2f * (fraction - 0.6f), dstPath, true)
                canvas.drawPath(dstPath, paint)
                canvas.drawArc(bgRectF!!, -105 + 360 * fraction, 360 * (1 - fraction), false, paint)
            }
            else -> {
                // 弹性部分
                dstPath.reset()
                pathMeasure.getSegment(10 * circleRadius * (fraction - 1), pathLength, dstPath, true)
                canvas.drawPath(dstPath, paint)
            }
        }
    }

    /**
     * 播放状态
     */
    fun play() {
        if (currentState == STATE_PLAY) {
            return
        }
        currentState = STATE_PLAY
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(1f, 100f)
        valueAnimator.duration = animDuration.toLong()
        valueAnimator.interpolator = AnticipateInterpolator()
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            fraction = 1 - animation.animatedFraction
            invalidate()
        }
        valueAnimator.start()
    }

    /**
     * 暂停状态
     */
    fun pause() {
        if (currentState == STATE_PAUSE) {
            return
        }
        currentState = STATE_PAUSE
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(1f, 100f)
        valueAnimator.duration = animDuration.toLong()
        valueAnimator.interpolator = AnticipateInterpolator()
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            fraction = animation.animatedFraction
            invalidate()
        }
        valueAnimator.start()
    }

    /**
     * 获取当前状态
     */
    fun getCurrentState(): Int {
        return currentState
    }

    /**
     * 设置动画时间
     */
    fun setAnimDuration(duration: Int) {
        animDuration = duration
    }

    /**
     * 设置线条颜色
     */
    fun setLineColor(color: Int) {
        paint.color = color
        invalidate()
    }

    /**
     * 设置线条大小
     */
    fun setLineSize(size: Int) {
        paint.strokeWidth = size.toFloat()
        invalidate()
    }
}