package com.hjq.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.hjq.smallest.width.dp2px
import com.hjq.smallest.width.sp2px
import com.hjq.widget.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/01/23
 *    desc   : 设置条自定义控件
 */
class SettingBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {

        /** 无色值 */
        const val NO_COLOR: Int = Color.TRANSPARENT
    }

    private val mainLayout: LinearLayout = LinearLayout(getContext())
    private val startView: TextView = TextView(getContext())
    private val endView: TextView = TextView(getContext())
    private val lineView: View = View(getContext())

    /** 图标着色器 */
    private var startDrawableTint: Int = 0
    private var endDrawableTint: Int = 0

    /** 图标显示大小 */
    private var startDrawableSize: Int = 0
    private var endDrawableSize: Int = 0

    init {
        mainLayout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL)

        val startLayoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT)
        startLayoutParams.gravity = Gravity.CENTER_VERTICAL
        startLayoutParams.weight = 1f
        startView.layoutParams = startLayoutParams

        val endLayoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        endLayoutParams.gravity = Gravity.CENTER_VERTICAL
        endView.layoutParams = endLayoutParams

        lineView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 1, Gravity.BOTTOM)

        startView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        endView.gravity = Gravity.END or Gravity.CENTER_VERTICAL

        startView.isSingleLine = true
        endView.isSingleLine = true

        startView.ellipsize = TextUtils.TruncateAt.END
        endView.ellipsize = TextUtils.TruncateAt.END

        startView.setLineSpacing(dp2px(5), startView.lineSpacingMultiplier)
        endView.setLineSpacing(dp2px(5), endView.lineSpacingMultiplier)

        startView.setPaddingRelative(dp2px(15).toInt(), dp2px(12).toInt(),
            dp2px(15).toInt(), dp2px(12).toInt())
        endView.setPaddingRelative(dp2px(15).toInt(), dp2px(12).toInt(),
            dp2px(15).toInt(), dp2px(12).toInt())

        val array: TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SettingBar)

        // 文本设置
        if (array.hasValue(R.styleable.SettingBar_bar_startText)) {
            setStartText(array.getString(R.styleable.SettingBar_bar_startText))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_endText)) {
            setEndText(array.getString(R.styleable.SettingBar_bar_endText))
        }

        // 提示设置
        if (array.hasValue(R.styleable.SettingBar_bar_startTextHint)) {
            setStartTextHint(array.getString(R.styleable.SettingBar_bar_startTextHint))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_endTextHint)) {
            setEndTextHint(array.getString(R.styleable.SettingBar_bar_endTextHint))
        }

        // 图标显示的大小
        if (array.hasValue(R.styleable.SettingBar_bar_startDrawableSize)) {
            setStartDrawableSize(array.getDimensionPixelSize(R.styleable.SettingBar_bar_startDrawableSize, 0))
        }

        if (array.hasValue(R.styleable.SettingBar_bar_endDrawableSize)) {
            setEndDrawableSize(array.getDimensionPixelSize(R.styleable.SettingBar_bar_endDrawableSize, 0))
        }

        // 图标着色器
        if (array.hasValue(R.styleable.SettingBar_bar_startDrawableTint)) {
            setStartDrawableTint(array.getColor(R.styleable.SettingBar_bar_startDrawableTint, NO_COLOR))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_endDrawableTint)) {
            setEndDrawableTint(array.getColor(R.styleable.SettingBar_bar_endDrawableTint, NO_COLOR))
        }

        // 图标和文字之间的间距
        setStartDrawablePadding(
            if (array.hasValue(R.styleable.SettingBar_bar_startDrawablePadding)) array.getDimensionPixelSize(
                R.styleable.SettingBar_bar_startDrawablePadding,
                0
            ) else dp2px(10).toInt()
        )
        setEndDrawablePadding(
            if (array.hasValue(R.styleable.SettingBar_bar_endDrawablePadding))
                array.getDimensionPixelSize(R.styleable.SettingBar_bar_endDrawablePadding, 0)
            else
                dp2px(10).toInt()
        )

        // 图标设置
        if (array.hasValue(R.styleable.SettingBar_bar_startDrawable)) {
            setStartDrawable(array.getDrawable(R.styleable.SettingBar_bar_startDrawable))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_endDrawable)) {
            setEndDrawable(array.getDrawable(R.styleable.SettingBar_bar_endDrawable))
        }

        // 文字颜色设置
        setStartTextColor(array.getColor(R.styleable.SettingBar_bar_startTextColor, ContextCompat.getColor(getContext(), R.color.black80)))
        setEndTextColor(array.getColor(R.styleable.SettingBar_bar_endTextColor, ContextCompat.getColor(getContext(), R.color.black60)))

        // 文字大小设置
        setStartTextSize(TypedValue.COMPLEX_UNIT_PX, array.getDimensionPixelSize(
            R.styleable.SettingBar_bar_startTextSize, sp2px(15).toInt()).toFloat())
        setEndTextSize(TypedValue.COMPLEX_UNIT_PX, array.getDimensionPixelSize(
            R.styleable.SettingBar_bar_endTextSize, sp2px(14).toInt()).toFloat())

        // 分割线设置
        if (array.hasValue(R.styleable.SettingBar_bar_lineDrawable)) {
            setLineDrawable(array.getDrawable(R.styleable.SettingBar_bar_lineDrawable))
        } else {
            setLineDrawable(ColorDrawable(Color.parseColor("#ECECEC")))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_lineVisible)) {
            setLineVisible(array.getBoolean(R.styleable.SettingBar_bar_lineVisible, true))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_lineSize)) {
            setLineSize(array.getDimensionPixelSize(R.styleable.SettingBar_bar_lineSize, 0))
        }
        if (array.hasValue(R.styleable.SettingBar_bar_lineMargin)) {
            setLineMargin(array.getDimensionPixelSize(R.styleable.SettingBar_bar_lineMargin, 0))
        }

        if (background == null) {
            val drawable = StateListDrawable()
            drawable.addState(intArrayOf(android.R.attr.state_pressed),
                ColorDrawable(ContextCompat.getColor(getContext(), R.color.black5)))
            drawable.addState(intArrayOf(android.R.attr.state_selected),
                ColorDrawable(ContextCompat.getColor(getContext(), R.color.black5)))
            drawable.addState(intArrayOf(android.R.attr.state_focused),
                ColorDrawable(ContextCompat.getColor(getContext(), R.color.black5)))
            drawable.addState(intArrayOf(),
                ColorDrawable(ContextCompat.getColor(getContext(), R.color.white)))
            background = drawable

            // 必须要设置可点击，否则点击屏幕任何角落都会触发按压事件
            isFocusable = true
            isClickable = true
        }
        array.recycle()


        // 适配 RTL 特性
        if (startView.textAlignment == TEXT_ALIGNMENT_GRAVITY) {
            startView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        }

        mainLayout.addView(startView)
        mainLayout.addView(endView)

        addView(mainLayout, 0)
        addView(lineView, 1)

        mainLayout.addOnLayoutChangeListener(object : OnLayoutChangeListener {

            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int,
                                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                v?.removeOnLayoutChangeListener(this)
                // 限制右边 View 的宽度，避免文本过长挤掉左边 View
                endView.maxWidth = (right - left) / 3 * 2
            }
        })
    }

    /**
     * 设置左边的文本
     */
    fun setStartText(@StringRes id: Int): SettingBar = apply {
        setStartText(resources.getString(id))
    }

    fun setStartText(text: CharSequence?): SettingBar = apply {
        startView.text = text
    }

    fun getStartText(): CharSequence? {
        return startView.text
    }

    /**
     * 设置左边的提示
     */
    fun setStartTextHint(@StringRes id: Int): SettingBar = apply {
        setStartTextHint(resources.getString(id))
    }

    fun setStartTextHint(hint: CharSequence?): SettingBar = apply {
        startView.hint = hint
    }

    /**
     * 设置右边的标题
     */
    fun setEndText(@StringRes id: Int): SettingBar = apply {
        setEndText(resources.getString(id))
    }

    fun setEndText(text: CharSequence?): SettingBar = apply {
        endView.text = text
    }

    fun getEndText(): CharSequence? {
        return endView.text
    }

    /**
     * 设置右边的提示
     */
    fun setEndTextHint(@StringRes id: Int): SettingBar = apply {
        setEndTextHint(resources.getString(id))
    }

    fun setEndTextHint(hint: CharSequence?): SettingBar = apply {
        endView.hint = hint
    }

    /**
     * 设置左边的图标
     */
    fun setStartDrawable(@DrawableRes id: Int): SettingBar = apply {
        setStartDrawable(ContextCompat.getDrawable(context, id))
    }

    fun setStartDrawable(drawable: Drawable?): SettingBar = apply {
        startView.setCompoundDrawablesRelative(drawable, null, null, null)
        setStartDrawableSize(startDrawableSize)
        setStartDrawableTint(startDrawableTint)
    }

    fun getStartDrawable(): Drawable? {
        return startView.getCompoundDrawablesRelative()[0]
    }

    /**
     * 设置右边的图标
     */
    fun setEndDrawable(@DrawableRes id: Int): SettingBar = apply {
        setEndDrawable(ContextCompat.getDrawable(context, id))
    }

    fun setEndDrawable(drawable: Drawable?): SettingBar = apply {
        endView.setCompoundDrawablesRelative(null, null, drawable, null)
        setEndDrawableSize(endDrawableSize)
        setEndDrawableTint(endDrawableTint)
    }

    fun getEndDrawable(): Drawable? {
        return endView.getCompoundDrawablesRelative()[2]
    }

    /**
     * 设置左边的图标间距
     */
    fun setStartDrawablePadding(padding: Int): SettingBar = apply {
        startView.compoundDrawablePadding = padding
    }

    /**
     * 设置右边的图标间距
     */
    fun setEndDrawablePadding(padding: Int): SettingBar = apply {
        endView.compoundDrawablePadding = padding
    }

    /**
     * 设置左边的图标大小
     */
    fun setStartDrawableSize(size: Int): SettingBar = apply {
        startDrawableSize = size
        val drawable: Drawable? = getStartDrawable()
        if (drawable != null) {
            if (size > 0) {
                drawable.setBounds(0, 0, size, size)
            } else {
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            }
            startView.setCompoundDrawablesRelative(drawable, null, null, null)
        }
    }

    /**
     * 设置右边的图标大小
     */
    fun setEndDrawableSize(size: Int): SettingBar = apply {
        endDrawableSize = size
        val drawable: Drawable? = getEndDrawable()
        if (drawable != null) {
            if (size > 0) {
                drawable.setBounds(0, 0, size, size)
            } else {
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            }
            endView.setCompoundDrawablesRelative(null, null, drawable, null)
        }
    }

    /**
     * 设置左边的图标着色器
     */
    fun setStartDrawableTint(color: Int): SettingBar = apply {
        startDrawableTint = color
        val drawable: Drawable? = getStartDrawable()
        if (drawable != null && color != NO_COLOR) {
            drawable.mutate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            } else {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    /**
     * 设置右边的图标着色器
     */
    fun setEndDrawableTint(color: Int): SettingBar = apply {
        endDrawableTint = color
        val drawable: Drawable? = getEndDrawable()
        if (drawable != null && color != NO_COLOR) {
            drawable.mutate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            } else {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    /**
     * 设置左边的文本颜色
     */
    fun setStartTextColor(@ColorInt color: Int): SettingBar = apply {
        startView.setTextColor(color)
    }

    /**
     * 设置右边的文本颜色
     */
    fun setEndTextColor(@ColorInt color: Int): SettingBar = apply {
        endView.setTextColor(color)
    }

    /**
     * 设置左边的文字大小
     */
    fun setStartTextSize(unit: Int, size: Float): SettingBar = apply {
        startView.setTextSize(unit, size)
    }

    /**
     * 设置右边的文字大小
     */
    fun setEndTextSize(unit: Int, size: Float): SettingBar = apply {
        endView.setTextSize(unit, size)
    }

    /**
     * 设置分割线是否显示
     */
    fun setLineVisible(visible: Boolean): SettingBar = apply {
        lineView.visibility = if (visible) VISIBLE else GONE
    }

    /**
     * 设置分割线的颜色
     */
    fun setLineColor(@ColorInt color: Int): SettingBar = apply {
        setLineDrawable(ColorDrawable(color))
    }

    fun setLineDrawable(drawable: Drawable?): SettingBar = apply {
        lineView.background = drawable
    }

    /**
     * 设置分割线的大小
     */
    fun setLineSize(size: Int): SettingBar = apply {
        var params: LayoutParams? = lineView.layoutParams as LayoutParams?
        if (params == null) {
            params = generateDefaultLayoutParams()
        }
        params?.height = size
        lineView.layoutParams = params
    }

    /**
     * 设置分割线边界
     */
    fun setLineMargin(margin: Int): SettingBar = apply {
        var params: LayoutParams? = lineView.layoutParams as LayoutParams?
        if (params == null) {
            params = generateDefaultLayoutParams()
        }
        params?.leftMargin = margin
        params?.rightMargin = margin
        lineView.layoutParams = params
    }

    /**
     * 获取主布局
     */
    fun getMainLayout(): LinearLayout {
        return mainLayout
    }

    /**
     * 获取左 TextView
     */
    fun getStartView(): TextView {
        return startView
    }

    /**
     * 获取右 TextView
     */
    fun getEndView(): TextView {
        return endView
    }

    /**
     * 获取分割线
     */
    fun getLineView(): View {
        return lineView
    }
}