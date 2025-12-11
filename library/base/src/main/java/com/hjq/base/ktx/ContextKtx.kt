package com.hjq.base.ktx

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/07/02
 *    desc   : Context 扩展
 */

/* 获取 Activity 对象 */
fun Context.getActivity() : Activity? {
    var context: Context? = this
    do {
        when (context) {
            is Activity -> {
                return context
            }
            is ContextWrapper -> {
                context = context.baseContext
            }
            else -> {
                return null
            }
        }
    } while (context != null)
    return null
}