package com.hjq.widget.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.AccelerateInterpolator
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatButton
import com.hjq.widget.R
import kotlin.math.sqrt

/**
 *    author : Unstoppable & Android 轮子哥
 *    github : https://github.com/Someonewow/SubmitButton
 *    time   : 2016/12/31
 *    desc   : 带提交动画按钮
 */
class SubmitButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatButton(context, attrs, defStyleAttr) {

    companion object {

        /** 无进度 */
        private const val STYLE_LOADING: Int = 0x00

        /** 带进度 */
        private const val STYLE_PROGRESS: Int = 0x01

        /** 默认状态 */
        private const val STATE_NONE: Int = 0

        /** 提交状态 */
        private const val STATE_SUBMIT: Int = 1

        /** 加载状态 */
        private const val STATE_LOADING: Int = 2

        /** 结果状态 */
        private const val STATE_RESULT: Int = 3
    }

    /** 当前按钮状态 */
    private var buttonState: Int = STATE_NONE

    /** 当前进度条样式 */
    private val progressStyle: Int
    private var currentProgress: Float = 0f

    /** View 宽高 */
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    /** View 最大宽高 */
    private var maxViewWidth: Int = 0
    private var maxViewHeight: Int = 0

    /** 画布坐标原点 */
    private var canvasX: Int = 0
    private var canvasY: Int = 0

    /** 进度按钮的颜色 */
    private val progressColor: Int

    /** 成功按钮的颜色 */
    private val succeedColor: Int

    /** 失败按钮的颜色 */
    private val errorColor: Int
    private val backgroundPaint: Paint
    private val loadingPaint: Paint
    private val resultPaint: Paint
    private val buttonPath: Path
    private val loadPath: Path
    private val dstPath: Path
    private val pathMeasure: PathMeasure
    private val resultPath: Path
    private val circleLeft: RectF
    private val circleMid: RectF
    private val circleRight: RectF
    private var loadValue: Float = 0f
    private var submitAnim: ValueAnimator? = null
    private var loadingAnim: ValueAnimator? = null
    private var resultAnim: ValueAnimator? = null

    /** 是否有结果 */
    private var doResult: Boolean = false

    /** 是否成功了 */
    private var succeed: Boolean = false

    init {
        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SubmitButton, defStyleAttr, 0)
        progressColor = typedArray.getColor(R.styleable.SubmitButton_progressColor, getAccentColor())
        succeedColor = typedArray.getColor(R.styleable.SubmitButton_succeedColor, Color.parseColor("#19CC95"))
        errorColor = typedArray.getColor(R.styleable.SubmitButton_errorColor, Color.parseColor("#FC8E34"))
        progressStyle = typedArray.getInt(R.styleable.SubmitButton_progressStyle, STYLE_LOADING)
        typedArray.recycle()

        backgroundPaint = Paint()
        loadingPaint = Paint()
        resultPaint = Paint()
        buttonPath = Path()
        loadPath = Path()
        resultPath = Path()
        dstPath = Path()
        circleMid = RectF()
        circleLeft = RectF()
        circleRight = RectF()
        pathMeasure = PathMeasure()

        resetPaint()
    }

    /**
     * 重置画笔
     */
    private fun resetPaint() {
        backgroundPaint.color = progressColor
        backgroundPaint.strokeWidth = 5f
        backgroundPaint.isAntiAlias = true
        loadingPaint.color = progressColor
        loadingPaint.style = Paint.Style.STROKE
        loadingPaint.strokeWidth = 9f
        loadingPaint.isAntiAlias = true
        resultPaint.color = Color.WHITE
        resultPaint.style = Paint.Style.STROKE
        resultPaint.strokeWidth = 9f
        resultPaint.strokeCap = Paint.Cap.ROUND
        resultPaint.isAntiAlias = true
        buttonPath.reset()
        loadPath.reset()
        resultPath.reset()
        dstPath.reset()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        // 当前必须不是在动画执行过程中
        if (buttonState != STATE_LOADING) {
            viewWidth = width - 10
            viewHeight = height - 10
            canvasX = (width * 0.5).toInt()
            canvasY = (height * 0.5).toInt()
            maxViewWidth = viewWidth
            maxViewHeight = viewHeight
        }
    }

    override fun onDraw(canvas: Canvas) {
        when (buttonState) {
            STATE_NONE -> {
                super.onDraw(canvas)
            }
            STATE_SUBMIT, STATE_LOADING -> {
                // 清除画布之前绘制的背景
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                canvas.translate(canvasX.toFloat(), canvasY.toFloat())
                drawButton(canvas)
                drawLoading(canvas)
            }
            STATE_RESULT -> {
                // 清除画布之前绘制的背景
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                canvas.translate(canvasX.toFloat(), canvasY.toFloat())
                drawButton(canvas)
                drawResult(canvas, succeed)
            }
        }
    }

    /**
     * 绘制按钮
     */
    private fun drawButton(canvas: Canvas) {
        buttonPath.reset()
        circleLeft.set(-viewWidth / 2f, -viewHeight / 2f, -viewWidth / 2f + viewHeight, viewHeight / 2f)
        buttonPath.arcTo(circleLeft, 90f, 180f)
        buttonPath.lineTo(viewWidth / 2f - viewHeight / 2f, -viewHeight / 2f)
        circleRight.set(viewWidth / 2f - viewHeight, -viewHeight / 2f, viewWidth / 2f, viewHeight / 2f)
        buttonPath.arcTo(circleRight, 270f, 180f)
        buttonPath.lineTo(-viewWidth / 2f + viewHeight / 2f, viewHeight / 2f)
        canvas.drawPath(buttonPath, backgroundPaint)
    }

    /**
     * 绘制加载转圈
     */
    private fun drawLoading(canvas: Canvas) {
        dstPath.reset()
        circleMid.set(-maxViewHeight / 2f, -maxViewHeight / 2f, maxViewHeight / 2f, maxViewHeight / 2f)
        loadPath.addArc(circleMid, 270f, 359.999f)
        pathMeasure.setPath(loadPath, true)
        var startD = 0f
        val stopD: Float
        if (progressStyle == STYLE_LOADING) {
            startD = pathMeasure.length * loadValue
            stopD = startD + pathMeasure.length / 2 * loadValue
        } else {
            stopD = pathMeasure.length * currentProgress
        }
        pathMeasure.getSegment(startD, stopD, dstPath, true)
        canvas.drawPath(dstPath, loadingPaint)
    }

    /**
     * 绘制结果按钮
     */
    private fun drawResult(canvas: Canvas, isSucceed: Boolean) {
        if (isSucceed) {
            resultPath.moveTo(-viewHeight / 6f, 0f)
            resultPath.lineTo(0f, (-viewHeight / 6 + (1 + sqrt(5.0)) * viewHeight / 12).toFloat())
            resultPath.lineTo(viewHeight / 6f, -viewHeight / 6f)
        } else {
            resultPath.moveTo(-viewHeight / 6f, viewHeight / 6f)
            resultPath.lineTo(viewHeight / 6f, -viewHeight / 6f)
            resultPath.moveTo(-viewHeight / 6f, -viewHeight / 6f)
            resultPath.lineTo(viewHeight / 6f, viewHeight / 6f)
        }
        canvas.drawPath(resultPath, resultPaint)
    }

    /**
     * 开始提交动画
     */
    private fun startSubmitAnim() {
        buttonState = STATE_SUBMIT
        submitAnim = ValueAnimator.ofInt(maxViewWidth, maxViewHeight).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    if (doResult) {
                        startResultAnim()
                    } else {
                        startLoadingAnim()
                    }
                }
            })
            addUpdateListener { animation: ValueAnimator ->
                viewWidth = animation.animatedValue as Int
                if (viewWidth == viewHeight) {
                    backgroundPaint.color = Color.parseColor("#DDDDDD")
                    backgroundPaint.style = Paint.Style.STROKE
                }
                invalidate()
            }
            start()
        }
    }

    /**
     * 开始加载动画
     */
    private fun startLoadingAnim() {
        buttonState = STATE_LOADING
        if (progressStyle == STYLE_PROGRESS) {
            return
        }
        loadingAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation: ValueAnimator ->
                loadValue = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * 开始结果动画
     */
    private fun startResultAnim() {
        buttonState = STATE_RESULT
        loadingAnim?.cancel()
        resultAnim = ValueAnimator.ofInt(maxViewHeight, maxViewWidth).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    // 请求重新测量自身，因为 onMeasure 方法中避开了动画执行中获取 View 宽高
                    requestLayout()
                }
            })
            addUpdateListener { animation: ValueAnimator ->
                viewWidth = animation.animatedValue as Int
                resultPaint.alpha = ((viewWidth - viewHeight) * 255) / (maxViewWidth - maxViewHeight)
                if (viewWidth == viewHeight) {
                    if (succeed) {
                        backgroundPaint.color = succeedColor
                    } else {
                        backgroundPaint.color = errorColor
                    }
                    backgroundPaint.style = Paint.Style.FILL_AND_STROKE
                }
                invalidate()
            }
            start()
        }
    }

    override fun performClick(): Boolean {
        if (buttonState == STATE_NONE) {
            startSubmitAnim()
            return super.performClick()
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        submitAnim?.cancel()
        loadingAnim?.cancel()
        resultAnim?.cancel()
    }

    /**
     * 显示进度
     */
    fun showProgress() {
        if (buttonState == STATE_NONE) {
            startSubmitAnim()
        }
    }

    /**
     * 显示成功
     */
    fun showSucceed() {
        showResult(true)
    }

    /**
     * 显示错误
     */
    fun showError() {
        showResult(false)
    }

    /**
     * 显示错误之后延迟重置
     */
    fun showError(delayMillis: Long) {
        showResult(false)
        postDelayed({ reset() }, delayMillis)
    }

    /**
     * 显示提交结果
     */
    private fun showResult(succeed: Boolean) {
        if ((buttonState == STATE_NONE) || (buttonState == STATE_RESULT) || doResult) {
            return
        }
        doResult = true
        this.succeed = succeed
        if (buttonState == STATE_LOADING) {
            startResultAnim()
        }
    }

    /**
     * 重置按钮的状态
     */
    fun reset() {
        submitAnim?.cancel()
        loadingAnim?.cancel()
        resultAnim?.cancel()
        buttonState = STATE_NONE
        viewWidth = maxViewWidth
        viewHeight = maxViewHeight
        succeed = false
        doResult = false
        currentProgress = 0f
        resetPaint()
        invalidate()
    }

    /**
     * 设置按钮进度
     */
    fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        currentProgress = progress
        if (progressStyle == STYLE_PROGRESS && buttonState == STATE_LOADING) {
            invalidate()
        }
    }

    /**
     * 获取当前主题的强调色
     */
    private fun getAccentColor(): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        return typedValue.data
    }
}