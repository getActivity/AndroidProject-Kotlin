package com.hjq.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.annotation.StringRes
import com.hjq.widget.R
import kotlin.math.max

/**
 *    author : HaoZhang & Android 轮子哥
 *    github : https://github.com/HeZaiJin/SlantedTextView
 *    time   : 2016/06/30
 *    desc   : 一个倾斜的 TextView，适用于标签效果
 */
@Suppress("RtlHardcoded")
class SlantedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    companion object {

        /** 旋转角度 */
        const val ROTATE_ANGLE: Int = 45
    }

    /** 背景画笔 */
    private val backgroundPaint: Paint = Paint()

    /** 文字画笔 */
    private val textPaint: TextPaint

    /** 显示的文本 */
    private var text: String = ""

    /** 倾斜重心 */
    private var gravity: Int = 0

    /** 是否绘制成三角形的 */
    private var triangle: Boolean = false

    /** 背景颜色 */
    private var colorBackground: Int = 0

    /** 文字测量范围装载 */
    private val textBounds: Rect = Rect()

    /** 测量出来的文本高度 */
    private var textHeight: Int = 0

    init {
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        backgroundPaint.isAntiAlias = true
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.isAntiAlias = true
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SlantedTextView)
        setText(array.getString(R.styleable.SlantedTextView_android_text))
        setTextSize(TypedValue.COMPLEX_UNIT_PX, array.getDimensionPixelSize(R.styleable.SlantedTextView_android_textSize,
            resources.getDimension(R.dimen.sp_12).toInt()).toFloat())
        setTextColor(array.getColor(R.styleable.SlantedTextView_android_textColor, Color.WHITE))
        setTextStyle(Typeface.defaultFromStyle(array.getInt(R.styleable.SlantedTextView_android_textStyle, Typeface.NORMAL)))
        setGravity(array.getInt(R.styleable.SlantedTextView_android_gravity, Gravity.END))
        setColorBackground(array.getColor(R.styleable.SlantedTextView_android_colorBackground, getAccentColor()))
        setTriangle(array.getBoolean(R.styleable.SlantedTextView_triangle, false))
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        textHeight = textBounds.height() + paddingTop + paddingBottom
        var width = 0
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> width = MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> width =
                textBounds.width() + paddingLeft + paddingRight
        }
        var height = 0
        when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> {
                height = MeasureSpec.getSize(heightMeasureSpec)
            }
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                height = textBounds.height() + paddingTop + paddingBottom
            }
        }
        setMeasuredDimension(max(width, height), max(width, height))
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        drawText(canvas)
    }

    /**
     * 绘制背景
     */
    private fun drawBackground(canvas: Canvas) {
        val path = Path()
        val width: Int = canvas.width
        val height: Int = canvas.height
        when (gravity) {
            Gravity.LEFT, Gravity.LEFT or Gravity.TOP -> {
                if (triangle) {
                    path.lineTo(0f, height.toFloat())
                    path.lineTo(width.toFloat(), 0f)
                } else {
                    path.moveTo(width.toFloat(), 0f)
                    path.lineTo(0f, height.toFloat())
                    path.lineTo(0f, (height - textHeight).toFloat())
                    path.lineTo((width - textHeight).toFloat(), 0f)
                }
            }
            Gravity.NO_GRAVITY, Gravity.RIGHT, Gravity.RIGHT or Gravity.TOP -> {
                if (triangle) {
                    path.lineTo(width.toFloat(), 0f)
                    path.lineTo(width.toFloat(), height.toFloat())
                } else {
                    path.lineTo(width.toFloat(), height.toFloat())
                    path.lineTo(width.toFloat(), (height - textHeight).toFloat())
                    path.lineTo(textHeight * 1f, 0f)
                }
            }
            Gravity.BOTTOM, Gravity.LEFT or Gravity.BOTTOM -> {
                if (triangle) {
                    path.lineTo(width.toFloat(), height.toFloat())
                    path.lineTo(0f, height.toFloat())
                } else {
                    path.lineTo(width.toFloat(), height.toFloat())
                    path.lineTo((width - textHeight).toFloat(), height.toFloat())
                    path.lineTo(0f, textHeight.toFloat())
                }
            }
            Gravity.RIGHT or Gravity.BOTTOM -> {
                if (triangle) {
                    path.moveTo(0f, height.toFloat())
                    path.lineTo(width.toFloat(), height.toFloat())
                    path.lineTo(width.toFloat(), 0f)
                } else {
                    path.moveTo(0f, height.toFloat())
                    path.lineTo(textHeight * 1f, height.toFloat())
                    path.lineTo(width.toFloat(), textHeight.toFloat())
                    path.lineTo(width.toFloat(), 0f)
                }
            }
            else -> {
                throw IllegalArgumentException("are you ok?")
            }
        }
        path.close()
        canvas.drawPath(path, backgroundPaint)
        canvas.save()
    }

    /**
     * 绘制文本
     */
    private fun drawText(canvas: Canvas) {
        val width: Int = canvas.width - textHeight / 2
        val height: Int = canvas.height - textHeight / 2
        val rect: Rect?
        val rectF: RectF?
        val offset: Int = textHeight / 2
        val toX: Float
        val toY: Float
        val centerX: Float
        val centerY: Float
        val angle: Float
        when (gravity) {
            Gravity.LEFT, Gravity.LEFT or Gravity.TOP -> {
                rect = Rect(0, 0, width, height)
                rectF = RectF(rect)
                rectF.right = textPaint.measureText(text, 0, text.length)
                rectF.bottom = textPaint.descent() - textPaint.ascent()
                rectF.left += (rect.width() - rectF.right) / 2.0f
                rectF.top += (rect.height() - rectF.bottom) / 2.0f
                toX = rectF.left
                toY = rectF.top - textPaint.ascent()
                centerX = width / 2f
                centerY = height / 2f
                angle = -ROTATE_ANGLE.toFloat()
            }
            Gravity.NO_GRAVITY, Gravity.RIGHT, Gravity.RIGHT or Gravity.TOP -> {
                rect = Rect(offset, 0, width + offset, height)
                rectF = RectF(rect)
                rectF.right = textPaint.measureText(text, 0, text.length)
                rectF.bottom = textPaint.descent() - textPaint.ascent()
                rectF.left += (rect.width() - rectF.right) / 2.0f
                rectF.top += (rect.height() - rectF.bottom) / 2.0f
                toX = rectF.left
                toY = rectF.top - textPaint.ascent()
                centerX = width / 2f + offset
                centerY = height / 2f
                angle = ROTATE_ANGLE.toFloat()
            }
            Gravity.BOTTOM, Gravity.LEFT or Gravity.BOTTOM -> {
                rect = Rect(0, offset, width, height + offset)
                rectF = RectF(rect)
                rectF.right = textPaint.measureText(text, 0, text.length)
                rectF.bottom = textPaint.descent() - textPaint.ascent()
                rectF.left += (rect.width() - rectF.right) / 2.0f
                rectF.top += (rect.height() - rectF.bottom) / 2.0f
                toX = rectF.left
                toY = rectF.top - textPaint.ascent()
                centerX = width / 2f
                centerY = height / 2f + offset
                angle = ROTATE_ANGLE.toFloat()
            }
            Gravity.RIGHT or Gravity.BOTTOM -> {
                rect = Rect(offset, offset, width + offset, height + offset)
                rectF = RectF(rect)
                rectF.right = textPaint.measureText(text, 0, text.length)
                rectF.bottom = textPaint.descent() - textPaint.ascent()
                rectF.left += (rect.width() - rectF.right) / 2.0f
                rectF.top += (rect.height() - rectF.bottom) / 2.0f
                toX = rectF.left
                toY = rectF.top - textPaint.ascent()
                centerX = width / 2f + offset
                centerY = height / 2f + offset
                angle = -ROTATE_ANGLE.toFloat()
            }
            else -> {
                throw IllegalArgumentException("are you ok?")
            }
        }
        canvas.rotate(angle, centerX, centerY)
        canvas.drawText(text, toX, toY, textPaint)
    }

    /**
     * 获取显示文本
     */
    fun getText(): String {
        return text
    }

    /**
     * 设置显示文本
     */
    fun setText(@StringRes id: Int) {
        setText(resources.getString(id))
    }

    fun setText(text: String?) {
        val finalText = text ?: ""
        if (!TextUtils.equals(finalText, getText())) {
            this.text = finalText
            invalidate()
        }
    }

    /**
     * 获取字体颜色
     */
    fun getTextColor(): Int {
        return textPaint.color
    }

    /**
     * 设置字体颜色
     */
    fun setTextColor(color: Int) {
        if (getTextColor() != color) {
            textPaint.color = color
            invalidate()
        }
    }

    /**
     * 获取字体大小
     */
    fun getTextSize(): Float {
        return textPaint.textSize
    }

    /**
     * 设置字体大小
     */
    fun setTextSize(size: Float) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    fun setTextSize(unit: Int, size: Float) {
        val textSize: Float = TypedValue.applyDimension(unit, size, resources.displayMetrics)
        if (getTextSize() != textSize) {
            textPaint.textSize = textSize
            invalidate()
        }
    }

    /**
     * 获取文本样式
     */
    fun getTextStyle(): Typeface? {
        return textPaint.typeface
    }

    /**
     * 设置文本样式
     */
    fun setTextStyle(tf: Typeface?) {
        if (getTextStyle() !== tf) {
            textPaint.typeface = tf
            invalidate()
        }
    }

    /**
     * 获取背景颜色
     */
    fun getColorBackground(): Int {
        return colorBackground
    }

    /**
     * 设置背景颜色
     */
    fun setColorBackground(color: Int) {
        if (getColorBackground() != color) {
            colorBackground = color
            backgroundPaint.color = colorBackground
            invalidate()
        }
    }

    /**
     * 获取倾斜重心
     */
    fun getGravity(): Int {
        return gravity
    }

    /**
     * 设置倾斜重心
     */
    fun setGravity(gravity: Int) {
        if (this.gravity != gravity) {
            // 适配布局反方向
            this.gravity = Gravity.getAbsoluteGravity(gravity, resources.configuration.layoutDirection)
            invalidate()
        }
    }

    /**
     * 当前是否是三角形
     */
    fun isTriangle(): Boolean {
        return triangle
    }

    /**
     * 是否设置成三角形
     */
    fun setTriangle(triangle: Boolean) {
        if (isTriangle() != triangle) {
            this.triangle = triangle
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