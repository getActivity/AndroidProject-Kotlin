package com.hjq.widget.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
 *    time   : 2019/08/25
 *    desc   : 密码隐藏显示 EditText
 */
@Suppress("ClickableViewAccessibility")
class PasswordEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle) :
    RegexEditText(context, attrs, defStyleAttr),
    OnTouchListener, OnFocusChangeListener, TextWatcher {

    private var currentDrawable: Drawable
    private val visibleDrawable: Drawable = DrawableCompat.wrap(requireNotNull(ContextCompat.getDrawable(context, R.drawable.password_off_ic)))
    private val invisibleDrawable: Drawable
    private var touchListener: OnTouchListener? = null
    private var focusChangeListener: OnFocusChangeListener? = null

    init {
        visibleDrawable.setBounds(0, 0, visibleDrawable.intrinsicWidth, visibleDrawable.intrinsicHeight)
        invisibleDrawable = DrawableCompat.wrap(requireNotNull(ContextCompat.getDrawable(context, R.drawable.password_on_ic)))
        invisibleDrawable.setBounds(0, 0, invisibleDrawable.intrinsicWidth, invisibleDrawable.intrinsicHeight)
        currentDrawable = visibleDrawable

        if (getInputRegex() == null) {
            // 密码输入规则
            setInputRegex(REGEX_PASSWORD)
        }
        setDrawableVisible(false)
        setPasswordMode(true)
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        super.addTextChangedListener(this)
    }

    private fun setDrawableVisible(visible: Boolean) {
        if (currentDrawable.isVisible == visible) {
            return
        }
        currentDrawable.setVisible(visible, false)
        val drawables: Array<Drawable?> = compoundDrawablesRelative
        setCompoundDrawablesRelative(drawables[0], drawables[1], if (visible) currentDrawable else null, drawables[3])
    }

    private fun setPasswordMode(passwordMode: Boolean) {
        transformationMethod = if (passwordMode) {
            // 密码不可见
            PasswordTransformationMethod.getInstance()
        } else {
            // 密码可见
            HideReturnsTransformationMethod.getInstance()
        }
    }

    private fun refreshDrawableStatus() {
        val drawables: Array<Drawable?> = compoundDrawablesRelative
        setCompoundDrawablesRelative(drawables[0], drawables[1], currentDrawable, drawables[3])
    }

    override fun setOnFocusChangeListener(onFocusChangeListener: OnFocusChangeListener?) {
        focusChangeListener = onFocusChangeListener
    }

    override fun setOnTouchListener(onTouchListener: OnTouchListener?) {
        touchListener = onTouchListener
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
        var touchDrawable = false
        // 获取布局方向
        val layoutDirection: Int = layoutDirection
        if (layoutDirection == LAYOUT_DIRECTION_LTR) {
            // 从左往右
            touchDrawable = x > width - currentDrawable.intrinsicWidth - paddingEnd && x < width - paddingEnd
        } else if (layoutDirection == LAYOUT_DIRECTION_RTL) {
            // 从右往左
            touchDrawable = x > paddingStart &&
                    x < paddingStart + currentDrawable.intrinsicWidth
        }
        if (currentDrawable.isVisible && touchDrawable) {
            if (event.action == MotionEvent.ACTION_UP) {
                if (currentDrawable === visibleDrawable) {
                    currentDrawable = invisibleDrawable
                    setPasswordMode(false)
                    refreshDrawableStatus()
                } else if (currentDrawable === invisibleDrawable) {
                    currentDrawable = visibleDrawable
                    setPasswordMode(true)
                    refreshDrawableStatus()
                }
                val editable: Editable? = text
                if (editable != null) {
                    setSelection(editable.toString().length)
                }
            }
            return true
        }
        return touchListener?.onTouch(view, event) == true
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