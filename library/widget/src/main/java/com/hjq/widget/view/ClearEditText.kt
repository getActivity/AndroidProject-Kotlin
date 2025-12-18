package com.hjq.widget.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.hjq.widget.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 带清除按钮的 EditText
 */
@Suppress("ClickableViewAccessibility")
class ClearEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle) :
    RegexEditText(context, attrs, defStyleAttr),
    OnTouchListener, OnFocusChangeListener, TextWatcher {

    private val clearDrawable: Drawable = DrawableCompat.wrap(requireNotNull(ContextCompat.getDrawable(context, R.drawable.input_delete_ic)))
    private var touchListener: OnTouchListener? = null
    private var focusChangeListener: OnFocusChangeListener? = null

    init {
        clearDrawable.setBounds(0, 0, clearDrawable.intrinsicWidth, clearDrawable.intrinsicHeight)
        setDrawableVisible(false)
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        super.addTextChangedListener(this)

        // 适配 RTL 特性
        if (textAlignment == TEXT_ALIGNMENT_GRAVITY) {
            textAlignment = TEXT_ALIGNMENT_VIEW_START
        }
    }

    private fun setDrawableVisible(visible: Boolean) {
        if (clearDrawable.isVisible == visible) {
            return
        }
        clearDrawable.setVisible(visible, false)
        val drawables: Array<Drawable> = compoundDrawablesRelative
        setCompoundDrawablesRelative(drawables[0], drawables[1],
            if (visible) clearDrawable else null, drawables[3])
    }

    override fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        focusChangeListener = listener
    }

    override fun setOnTouchListener(listener: OnTouchListener?) {
        touchListener = listener
    }

    /**
     * [OnFocusChangeListener]
     */
    override fun onFocusChange(view: View, hasFocus: Boolean) {
        setDrawableVisible(hasFocus && !TextUtils.isEmpty(text))
        focusChangeListener?.onFocusChange(view, hasFocus)
    }

    /**
     * [OnTouchListener]
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val x: Int = event.x.toInt()
        // 是否触摸了 Drawable
        val touchDrawable = if (resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
            x > paddingStart && x < paddingStart + clearDrawable.intrinsicWidth
        } else {
            x > width - clearDrawable.intrinsicWidth - paddingEnd && x < width - paddingEnd
        }
        if (clearDrawable.isVisible && touchDrawable) {
            if (event.action == MotionEvent.ACTION_UP) {
                setText("")
            }
            return true
        }
        return touchListener?.onTouch(view, event) ?: false
    }

    /**
     * [TextWatcher]
     */
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (isFocused) {
            setDrawableVisible(s.isNotEmpty())
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // default implementation ignored
    }

    override fun afterTextChanged(s: Editable) {
        // default implementation ignored
    }
}