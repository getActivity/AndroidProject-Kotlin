package com.hjq.widget.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/09/12
 *    desc   : 长按半透明松手恢复的 TextView
 */
class PressAlphaTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    override fun dispatchSetPressed(pressed: Boolean) {
        // 判断当前手指是否按下了
        alpha = if (pressed) 0.5f else 1f
    }
}