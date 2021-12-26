package com.hjq.demo.other

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.hjq.demo.R
import kotlin.math.max
import kotlin.math.min

/**
 *    author : 王浩 & Android 轮子哥
 *    github : https://github.com/bingoogolapple/BGATransformersTip-Android
 *    time   : 2019/08/19
 *    desc   : 带箭头背景的 Drawable
 */
@Suppress("RtlHardcoded")
class ArrowDrawable private constructor(private val builder: Builder) : Drawable() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var path: Path = Path()

    override fun draw(canvas: Canvas) {
        if (builder.shadowSize > 0) {
            paint.maskFilter = BlurMaskFilter(builder.shadowSize.toFloat(), BlurMaskFilter.Blur.OUTER)
            paint.color = builder.shadowColor
            canvas.drawPath(path, paint)
        }
        paint.maskFilter = null
        paint.color = builder.backgroundColor
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun onBoundsChange(viewRect: Rect) {
        path.reset()

        val excludeShadowRectF = RectF(viewRect)
        excludeShadowRectF.inset(builder.shadowSize.toFloat(), builder.shadowSize.toFloat())
        val centerPointF = PointF()
        when (builder.arrowOrientation) {
            Gravity.LEFT -> {
                excludeShadowRectF.left += builder.arrowHeight.toFloat()
                centerPointF.x = excludeShadowRectF.left
            }
            Gravity.RIGHT -> {
                excludeShadowRectF.right -= builder.arrowHeight.toFloat()
                centerPointF.x = excludeShadowRectF.right
            }
            Gravity.TOP -> {
                excludeShadowRectF.top += builder.arrowHeight.toFloat()
                centerPointF.y = excludeShadowRectF.top
            }
            Gravity.BOTTOM -> {
                excludeShadowRectF.bottom -= builder.arrowHeight.toFloat()
                centerPointF.y = excludeShadowRectF.bottom
            }
        }
        when (builder.arrowGravity) {
            Gravity.LEFT -> centerPointF.x = excludeShadowRectF.left + builder.arrowHeight
            Gravity.CENTER_HORIZONTAL -> centerPointF.x = viewRect.width() / 2f
            Gravity.RIGHT -> centerPointF.x = excludeShadowRectF.right - builder.arrowHeight
            Gravity.TOP -> centerPointF.y = excludeShadowRectF.top + builder.arrowHeight
            Gravity.CENTER_VERTICAL -> centerPointF.y = viewRect.height() / 2f
            Gravity.BOTTOM -> centerPointF.y = excludeShadowRectF.bottom - builder.arrowHeight
        }

        // 更新箭头偏移量
        centerPointF.x += builder.arrowOffsetX.toFloat()
        centerPointF.y += builder.arrowOffsetY.toFloat()
        when (builder.arrowGravity) {
            Gravity.LEFT, Gravity.RIGHT, Gravity.CENTER_HORIZONTAL -> {
                centerPointF.x = max(centerPointF.x, excludeShadowRectF.left + builder.radius + builder.arrowHeight)
                centerPointF.x = min(centerPointF.x, excludeShadowRectF.right - builder.radius - builder.arrowHeight)
            }
            Gravity.TOP, Gravity.BOTTOM, Gravity.CENTER_VERTICAL -> {
                centerPointF.y = max(centerPointF.y, excludeShadowRectF.top + builder.radius + builder.arrowHeight)
                centerPointF.y = min(centerPointF.y, excludeShadowRectF.bottom - builder.radius - builder.arrowHeight)
            }
        }
        when (builder.arrowOrientation) {
            Gravity.LEFT, Gravity.RIGHT -> {
                centerPointF.x = max(centerPointF.x, excludeShadowRectF.left)
                centerPointF.x = min(centerPointF.x, excludeShadowRectF.right)
            }
            Gravity.TOP, Gravity.BOTTOM -> {
                centerPointF.y = max(centerPointF.y, excludeShadowRectF.top)
                centerPointF.y = min(centerPointF.y, excludeShadowRectF.bottom)
            }
        }

        // 箭头区域（其实是旋转了 90 度后的正方形区域）
        val arrowPath = Path()
        arrowPath.moveTo(centerPointF.x - builder.arrowHeight, centerPointF.y)
        arrowPath.lineTo(centerPointF.x, centerPointF.y - builder.arrowHeight)
        arrowPath.lineTo(centerPointF.x + builder.arrowHeight, centerPointF.y)
        arrowPath.lineTo(centerPointF.x, centerPointF.y + builder.arrowHeight)
        arrowPath.close()
        path.addRoundRect(
            excludeShadowRectF,
            builder.radius.toFloat(),
            builder.radius.toFloat(),
            Path.Direction.CW
        )
        path.addPath(arrowPath)
        invalidateSelf()
    }

    class Builder constructor(private val context: Context) {

        /** 箭头高度 */
        var arrowHeight: Int

        /** 背景圆角大小 */
        var radius: Int

        /** 箭头方向 */
        var arrowOrientation: Int

        /** 箭头重心 */
        var arrowGravity: Int

        /** 箭头水平方向偏移 */
        var arrowOffsetX: Int

        /** 箭头垂直方向偏移 */
        var arrowOffsetY: Int

        /** 阴影大小 */
        var shadowSize: Int

        /** 背景颜色 */
        var backgroundColor: Int

        /** 阴影颜色 */
        var shadowColor: Int

        init {
            backgroundColor = ContextCompat.getColor(context, R.color.black)
            shadowColor = ContextCompat.getColor(context, R.color.black20)
            arrowHeight = context.resources.getDimension(R.dimen.dp_6).toInt()
            radius = context.resources.getDimension(R.dimen.dp_4).toInt()
            shadowSize = 0
            arrowOffsetX = 0
            arrowOffsetY = 0
            arrowOrientation = Gravity.NO_GRAVITY
            arrowGravity = Gravity.NO_GRAVITY
        }

        /**
         * 设置背景色
         */
        fun setBackgroundColor(@ColorInt color: Int): Builder = apply {
            backgroundColor = color
        }

        /**
         * 设置阴影色
         */
        fun setShadowColor(@ColorInt color: Int): Builder = apply {
            shadowColor = color
        }

        /**
         * 设置箭头高度
         */
        fun setArrowHeight(height: Int): Builder = apply {
            arrowHeight = height
        }

        /**
         * 设置浮窗圆角半径
         */
        fun setRadius(radius: Int): Builder = apply {
            this.radius = radius
        }

        /**
         * 设置箭头方向（左上右下）
         */
        fun setArrowOrientation(orientation: Int): Builder = apply {
            when (val finalOrientation: Int = Gravity.getAbsoluteGravity(orientation, context.resources.configuration.layoutDirection)) {
                Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM -> {
                    arrowOrientation = finalOrientation
                }
                else -> throw IllegalArgumentException("are you ok?")
            }
        }

        /**
         * 设置箭头布局重心
         */
        fun setArrowGravity(gravity: Int): Builder = apply {
            var finalGravity: Int = gravity
            finalGravity = Gravity.getAbsoluteGravity(
                finalGravity,
                context.resources.configuration.layoutDirection
            )
            if (finalGravity == Gravity.CENTER) {
                when (arrowOrientation) {
                    Gravity.LEFT, Gravity.RIGHT -> finalGravity = Gravity.CENTER_VERTICAL
                    Gravity.TOP, Gravity.BOTTOM -> finalGravity = Gravity.CENTER_HORIZONTAL
                }
            }
            when (finalGravity) {
                Gravity.LEFT, Gravity.RIGHT -> if (arrowOrientation == Gravity.LEFT || arrowOrientation == Gravity.RIGHT) {
                    throw IllegalArgumentException("are you ok?")
                }
                Gravity.TOP, Gravity.BOTTOM -> if (arrowOrientation == Gravity.TOP || arrowOrientation == Gravity.BOTTOM) {
                    throw IllegalArgumentException("are you ok?")
                }
                Gravity.CENTER_VERTICAL, Gravity.CENTER_HORIZONTAL -> {}
                else -> {
                    throw IllegalArgumentException("are you ok?")
                }
            }
            arrowGravity = finalGravity
        }

        /**
         * 设置箭头在 x 轴的偏移量
         */
        fun setArrowOffsetX(offsetX: Int): Builder = apply {
            arrowOffsetX = offsetX
        }

        /**
         * 设置箭头在 y 轴的偏移量
         */
        fun setArrowOffsetY(offsetY: Int): Builder = apply {
            arrowOffsetY = offsetY
        }

        /**
         * 设置阴影宽度
         */
        fun setShadowSize(size: Int): Builder = apply {
            shadowSize = size
        }

        /**
         * 构建 Drawable
         */
        fun build(): Drawable {
            if (arrowOrientation == Gravity.NO_GRAVITY || arrowGravity == Gravity.NO_GRAVITY) {
                // 必须要先设置箭头的方向及重心
                throw IllegalArgumentException("are you ok?")
            }
            return ArrowDrawable(this)
        }

        /**
         * 应用到 View
         */
        fun apply(view: View) {
            view.background = build()
            if (shadowSize > 0 || arrowHeight > 0) {
                if ((view.paddingTop == 0) && (view.bottom == 0) &&
                    (view.paddingLeft == 0) && (view.paddingRight == 0)) {

                    view.setPadding(shadowSize, shadowSize + arrowHeight, shadowSize, shadowSize)
                }
            }
        }
    }
}