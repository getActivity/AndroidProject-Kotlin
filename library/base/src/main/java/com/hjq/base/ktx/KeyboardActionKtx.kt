package com.hjq.base.ktx

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/03/10
 *    desc   : 软键盘意图扩展
 */

fun View.showKeyboard() {
    showKeyboard(this)
}

/**
 * 显示软键盘，需要先 requestFocus 获取焦点，如果是在 Activity Create，那么需要延迟一段时间
 */
fun Any.showKeyboard(view: View?) {
    if (view == null) {
        return
    }
    val manager: InputMethodManager = view.context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager? ?: return
    manager.showSoftInput(view, InputMethodManager.SHOW_FORCED)
}

fun View.hideKeyboard() {
    hideKeyboard(this)
}

/**
 * 隐藏软键盘
 */
fun Any.hideKeyboard(view: View?) {
    if (view == null) {
        return
    }
    val manager: InputMethodManager = view.context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager? ?: return
    manager.hideSoftInputFromWindow(view.windowToken, 0)
}