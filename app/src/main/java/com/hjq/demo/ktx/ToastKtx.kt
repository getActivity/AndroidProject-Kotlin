package com.hjq.demo.ktx

import androidx.annotation.StringRes
import com.hjq.toast.Toaster

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/03/10
 *    desc   : Toast 调用扩展
 */
fun toast(text: CharSequence?) {
    Toaster.show(text)
}

fun toast(@StringRes id: Int) {
    Toaster.show(id)
}

fun toast(`object`: Any?) {
    Toaster.show(`object`)
}