package com.hjq.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.hjq.widget.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/02/20
 *    desc   : 高仿 ios 开关按钮
 */
class SwitchButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val STATE_SWITCH_OFF: Int = 1
        private const val STATE_SWITCH_OFF2: Int = 2
        private const val STATE_SWITCH_ON: Int = 3
        private const val STATE_SWITCH_ON2: Int = 4
    }

    private val interpolator: AccelerateInterpolator = AccelerateInterpolator(2f)
    private val paint: Paint = Paint()
    private val backgroundPath: Path = Path()
    private val barPath: Path = Path()
    private val bound: RectF = RectF()
    private var anim1: Float = 0f
    private var anim2: Float = 0f
    private var shadowGradient: RadialGradient? = null

    /** 按钮宽高形状比率(0,1] 不推荐大幅度调整 */
    private val aspectRatio: Float = 0.68f

    /** (0,1] */
    private val animationSpeed: Float = 0.1f

    /** 上一个选中状态 */
    private var lastCheckedState: Int

    /** 当前的选中状态 */
    private var checkedState: Int
    private var canVisibleDrawing: Boolean = false

    /** 是否显示按钮阴影 */
    private var shadow: Boolean = false

    /** 是否选中 */
    private var checked: Boolean = false

    /** 开启状态背景色 */
    private var accentColor: Int = Color.parseColor("#4BD763")

    /** 开启状态按钮描边色 */
    private var primaryDarkColor: Int = Color.parseColor("#3AC652")

    /** 关闭状态描边色 */
    private var offColor: Int = Color.parseColor("#E3E3E3")

    /** 关闭状态按钮描边色 */
    private var offDarkColor: Int = Color.parseColor("#BFBFBF")

    /** 按钮阴影色 */
    private var shadowColor: Int = Color.parseColor("#333333")

    /** 监听器 */
    private var listener: OnCheckedChangeListener? = null
    private var actuallyDrawingAreaRight: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var scale: Float = 0f
    private var offset: Float = 0f
    private var radius: Float = 0f
    private var strokeWidth: Float = 0f
    private var viewWidth: Float = 0f
    private var viewLeft: Float = 0f
    private var viewRight: Float = 0f
    private var onLeftX: Float = 0f
    private var on2LeftX: Float = 0f
    private var off2LeftX: Float = 0f
    private var offLeftX: Float = 0f
    private var shadowReservedHeight: Float = 0f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton)
        checked = array.getBoolean(R.styleable.SwitchButton_android_checked, checked)
        isEnabled = array.getBoolean(R.styleable.SwitchButton_android_enabled, isEnabled)
        checkedState = if (checked) STATE_SWITCH_ON else STATE_SWITCH_OFF
        lastCheckedState = checkedState
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var finalWidthMeasureSpec: Int = widthMeasureSpec
        var finalHeightMeasureSpec: Int = heightMeasureSpec
        when (MeasureSpec.getMode(finalWidthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                finalWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    (resources.getDimension(R.dimen.dp_56) + paddingLeft + paddingRight).toInt(), MeasureSpec.EXACTLY)
            }
            MeasureSpec.EXACTLY -> {}
        }
        when (MeasureSpec.getMode(finalHeightMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        ((MeasureSpec.getSize(finalWidthMeasureSpec) * aspectRatio).toInt() + paddingTop + paddingBottom),
                    MeasureSpec.EXACTLY)
            }
            MeasureSpec.EXACTLY -> {}
        }
        setMeasuredDimension(finalWidthMeasureSpec, finalHeightMeasureSpec)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        canVisibleDrawing = width > paddingLeft + paddingRight && height > paddingTop + paddingBottom
        if (canVisibleDrawing) {
            val actuallyDrawingAreaWidth: Int = width - paddingLeft - paddingRight
            val actuallyDrawingAreaHeight: Int = height - paddingTop - paddingBottom
            val actuallyDrawingAreaLeft: Int
            val actuallyDrawingAreaRight: Int
            val actuallyDrawingAreaTop: Int
            val actuallyDrawingAreaBottom: Int
            if (actuallyDrawingAreaWidth * aspectRatio < actuallyDrawingAreaHeight) {
                actuallyDrawingAreaLeft = paddingLeft
                actuallyDrawingAreaRight = width - paddingRight
                val heightExtraSize: Int = (actuallyDrawingAreaHeight - actuallyDrawingAreaWidth * aspectRatio).toInt()
                actuallyDrawingAreaTop = paddingTop + heightExtraSize / 2
                actuallyDrawingAreaBottom = getHeight() - paddingBottom - (heightExtraSize / 2)
            } else {
                val widthExtraSize: Int = (actuallyDrawingAreaWidth - actuallyDrawingAreaHeight / aspectRatio).toInt()
                actuallyDrawingAreaLeft = paddingLeft + widthExtraSize / 2
                actuallyDrawingAreaRight = getWidth() - paddingRight - (widthExtraSize / 2)
                actuallyDrawingAreaTop = paddingTop
                actuallyDrawingAreaBottom = getHeight() - paddingBottom
            }
            shadowReservedHeight = ((actuallyDrawingAreaBottom - actuallyDrawingAreaTop) * 0.07f).toInt().toFloat()
            val left: Float = actuallyDrawingAreaLeft.toFloat()
            val top: Float = actuallyDrawingAreaTop + shadowReservedHeight
            this.actuallyDrawingAreaRight = actuallyDrawingAreaRight.toFloat()
            val bottom: Float = actuallyDrawingAreaBottom - shadowReservedHeight
            val sHeight: Float = bottom - top
            centerX = (this.actuallyDrawingAreaRight + left) / 2
            centerY = (bottom + top) / 2
            viewLeft = left
            viewWidth = bottom - top
            viewRight = left + viewWidth
            // OfB
            val halfHeightOfS: Float = viewWidth / 2
            radius = halfHeightOfS * 0.95f
            // offset of switching
            offset = radius * 0.2f
            strokeWidth = (halfHeightOfS - radius) * 2
            onLeftX = this.actuallyDrawingAreaRight - viewWidth
            on2LeftX = onLeftX - offset
            offLeftX = left
            off2LeftX = offLeftX + offset
            scale = 1 - strokeWidth / sHeight
            backgroundPath.reset()
            val bound = RectF()
            bound.top = top
            bound.bottom = bottom
            bound.left = left
            bound.right = left + sHeight
            backgroundPath.arcTo(bound, 90f, 180f)
            bound.left = this.actuallyDrawingAreaRight - sHeight
            bound.right = this.actuallyDrawingAreaRight
            backgroundPath.arcTo(bound, 270f, 180f)
            backgroundPath.close()
            this.bound.left = viewLeft
            this.bound.right = viewRight
            // bTop = sTop
            this.bound.top = top + strokeWidth / 2
            // bBottom = sBottom
            this.bound.bottom = bottom - strokeWidth / 2
            val bCenterX: Float = (viewRight + viewLeft) / 2
            val bCenterY: Float = (bottom + top) / 2
            val red: Int = shadowColor shr 16 and 0xFF
            val green: Int = shadowColor shr 8 and 0xFF
            val blue: Int = shadowColor and 0xFF
            shadowGradient = RadialGradient(bCenterX, bCenterY, radius, Color.argb(200, red, green, blue),
                Color.argb(25, red, green, blue), Shader.TileMode.CLAMP)
        }
    }

    private fun calcBPath(percent: Float) {
        barPath.reset()
        bound.left = viewLeft + strokeWidth / 2
        bound.right = viewRight - strokeWidth / 2
        barPath.arcTo(bound, 90f, 180f)
        bound.left = viewLeft + (percent * offset) + (strokeWidth / 2)
        bound.right = viewRight + percent * offset - strokeWidth / 2
        barPath.arcTo(bound, 270f, 180f)
        barPath.close()
    }

    private fun calcBTranslate(percent: Float): Float {
        var result = 0f
        when (checkedState - lastCheckedState) {
            1 -> {
                if (checkedState == STATE_SWITCH_OFF2) {
                    // off -> off2
                    result = offLeftX
                } else if (checkedState == STATE_SWITCH_ON) {
                    // on2 -> on
                    result = onLeftX - (onLeftX - on2LeftX) * percent
                }
            }
            2 -> {
                if (checkedState == STATE_SWITCH_ON) {
                    // off2 -> on
                    result = onLeftX - (onLeftX - offLeftX) * percent
                } else if (checkedState == STATE_SWITCH_ON2) {
                    // off -> on2
                    result = on2LeftX - (on2LeftX - offLeftX) * percent
                }
            }
            3 -> {
                // off -> on
                result = onLeftX - (onLeftX - offLeftX) * percent
            }
            -1 -> {
                if (checkedState == STATE_SWITCH_ON2) {
                    // on -> on2
                    result = on2LeftX + (onLeftX - on2LeftX) * percent
                } else if (checkedState == STATE_SWITCH_OFF) {
                    // off2 -> off
                    result = offLeftX
                }
            }
            -2 -> {
                if (checkedState == STATE_SWITCH_OFF) {
                    // on2 -> off
                    result = offLeftX + (on2LeftX - offLeftX) * percent
                } else if (checkedState == STATE_SWITCH_OFF2) {
                    // on -> off2
                    result = off2LeftX + (onLeftX - off2LeftX) * percent
                }
            }
            -3 -> {
                // on -> off
                result = offLeftX + (onLeftX - offLeftX) * percent
            }
            else -> {
                if (checkedState == STATE_SWITCH_OFF) {
                    //  off -> off
                    result = offLeftX
                } else if (checkedState == STATE_SWITCH_ON) {
                    // on -> on
                    result = onLeftX
                }
            }
        }
        return result - offLeftX
    }

    override fun onDraw(canvas: Canvas) {
        if (!canVisibleDrawing) {
            return
        }
        paint.isAntiAlias = true
        val isOn: Boolean = (checkedState == STATE_SWITCH_ON || checkedState == STATE_SWITCH_ON2)
        // Draw background
        paint.style = Paint.Style.FILL
        paint.color = if (isOn) accentColor else offColor
        canvas.drawPath(backgroundPath, paint)
        anim1 = if (anim1 - animationSpeed > 0) anim1 - animationSpeed else 0f
        anim2 = if (anim2 - animationSpeed > 0) anim2 - animationSpeed else 0f
        val dsAnim: Float = interpolator.getInterpolation(anim1)
        val dbAnim: Float = interpolator.getInterpolation(anim2)
        // Draw background animation
        val scale: Float = scale * (if (isOn) dsAnim else 1 - dsAnim)
        val scaleOffset: Float = (actuallyDrawingAreaRight - centerX - radius) * (if (isOn) 1 - dsAnim else dsAnim)
        canvas.save()
        canvas.scale(scale, scale, centerX + scaleOffset, centerY)
        if (isEnabled) {
            paint.color = Color.parseColor("#FFFFFF")
        } else {
            paint.color = Color.parseColor("#BBBBBB")
        }
        canvas.drawPath(backgroundPath, paint)
        canvas.restore()
        // To prepare center bar path
        canvas.save()
        canvas.translate(calcBTranslate(dbAnim), shadowReservedHeight)
        val isState2: Boolean =
            (checkedState == STATE_SWITCH_ON2 || checkedState == STATE_SWITCH_OFF2)
        calcBPath(if (isState2) 1 - dbAnim else dbAnim)
        // Use center bar path to draw shadow
        if (shadow) {
            paint.style = Paint.Style.FILL
            paint.shader = shadowGradient
            canvas.drawPath(barPath, paint)
            paint.shader = null
        }
        canvas.translate(0f, -shadowReservedHeight)
        // draw bar
        canvas.scale(0.98f, 0.98f, viewWidth / 2, viewWidth / 2)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FFFFFF")
        canvas.drawPath(barPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth * 0.5f
        paint.color = if (isOn) primaryDarkColor else offDarkColor
        canvas.drawPath(barPath, paint)
        canvas.restore()
        paint.reset()
        if (anim1 > 0 || anim2 > 0) {
            invalidate()
        }
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if ((isEnabled && (checkedState == STATE_SWITCH_ON || checkedState == STATE_SWITCH_OFF) && (anim1 * anim2 == 0f))) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    lastCheckedState = checkedState
                    anim2 = 1f
                    when (checkedState) {
                        STATE_SWITCH_OFF -> {
                            setChecked(checked = true, callback = false)
                            listener?.onCheckedChanged(this, true)
                        }
                        STATE_SWITCH_ON -> {
                            setChecked(checked = false, callback = false)
                            listener?.onCheckedChanged(this, false)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {}
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable? = super.onSaveInstanceState()
        val state = SavedState(superState)
        state.checked = checked
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState: SavedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        checked = savedState.checked
        checkedState = if (checked) STATE_SWITCH_ON else STATE_SWITCH_OFF
        invalidate()
    }

    fun setColor(newColorPrimary: Int, newColorPrimaryDark: Int) {
        setColor(newColorPrimary, newColorPrimaryDark, offColor, offDarkColor)
    }

    fun setColor(newColorPrimary: Int, newColorPrimaryDark: Int, newColorOff: Int, newColorOffDark: Int) {
        setColor(newColorPrimary, newColorPrimaryDark, newColorOff, newColorOffDark, shadowColor)
    }

    fun setColor(newColorPrimary: Int, newColorPrimaryDark: Int, newColorOff: Int, newColorOffDark: Int, newColorShadow: Int) {
        accentColor = newColorPrimary
        primaryDarkColor = newColorPrimaryDark
        offColor = newColorOff
        offDarkColor = newColorOffDark
        shadowColor = newColorShadow
        invalidate()
    }

    /**
     * 设置按钮阴影开关
     */
    fun setShadow(shadow: Boolean) {
        this.shadow = shadow
        invalidate()
    }

    /**
     * 当前状态是否选中
     */
    fun isChecked(): Boolean {
        return checked
    }

    /**
     * 设置选择状态（默认会回调监听器）
     */
    fun setChecked(checked: Boolean) {
        // 回调监听器
        setChecked(checked, true)
    }

    /**
     * 设置选择状态
     */
    fun setChecked(checked: Boolean, callback: Boolean) {
        val newState: Int = if (checked) STATE_SWITCH_ON else STATE_SWITCH_OFF
        if (newState == checkedState) {
            return
        }
        if (((newState == STATE_SWITCH_ON && (checkedState == STATE_SWITCH_OFF || checkedState == STATE_SWITCH_OFF2))
                    || (newState == STATE_SWITCH_OFF && (checkedState == STATE_SWITCH_ON || checkedState == STATE_SWITCH_ON2)))
        ) {
            anim1 = 1f
        }
        anim2 = 1f
        if (!this.checked && newState == STATE_SWITCH_ON) {
            this.checked = true
        } else if (this.checked && newState == STATE_SWITCH_OFF) {
            this.checked = false
        }
        lastCheckedState = checkedState
        checkedState = newState
        postInvalidate()
        if (callback) {
            listener?.onCheckedChanged(this, checked)
        }
    }

    /**
     * 设置选中状态改变监听
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
    }

    /**
     * 选中监听器
     */
    interface OnCheckedChangeListener {

        /**
         * 回调监听
         *
         * @param button            切换按钮
         * @param checked           是否选中
         */
        fun onCheckedChanged(button: SwitchButton, checked: Boolean)
    }

    /**
     * 保存开关状态
     */
    private class SavedState : BaseSavedState {

        var checked: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            checked = 1 == `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (checked) 1 else 0)
        }

        /**
         * fixed by Night99 https://github.com/g19980115
         */
        override fun describeContents(): Int {
            return 0
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> =
                object : Parcelable.Creator<SavedState?> {
                    override fun createFromParcel(`in`: Parcel): SavedState {
                        return SavedState(`in`)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }
}