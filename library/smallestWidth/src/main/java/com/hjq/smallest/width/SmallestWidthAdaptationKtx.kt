package com.hjq.smallest.width

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/03/10
 *    desc   : 最小宽度适配扩展
 */

/* dp 转 px 部分 */

fun Context.dp2px(value: Int): Float {
    return dp2px(this, value)
}

fun Fragment.dp2px(value: Int): Float {
    return dp2px(context, value)
}

fun View.dp2px(value: Int): Float {
    return dp2px(context, value)
}

fun Any.dp2px(context: Context?, value: Int): Float {
    if (context == null) {
        return 0f
    }
    return context.resources.getDimension(R.dimen.dp_1) * value.toFloat()
}

/* px 转 dp 部分 */

fun Context.px2dp(value: Float): Float {
    return px2dp(this, value)
}

fun Fragment.px2dp(value: Float): Float {
    return px2dp(context, value)
}

fun View.px2dp(value: Float): Float {
    return px2dp(context, value)
}

fun Any.px2dp(context: Context?, value: Float): Float {
    if (context == null) {
        return 0f
    }
    return value / context.resources.getDimension(R.dimen.dp_1)
}

/* sp 转 px 部分 */

fun Context.sp2px(value: Int): Float {
    return sp2px(this, value)
}

fun Fragment.sp2px(value: Int): Float {
    return sp2px(context, value)
}

fun View.sp2px(value: Int): Float {
    return sp2px(context, value)
}

fun Any.sp2px(context: Context?, value: Int): Float {
    if (context == null) {
        return 0f
    }
    return context.resources.getDimension(R.dimen.sp_10) / 10f * value.toFloat()
}

/* px 转 sp 部分 */

fun Context.px2sp(value: Float): Float {
    return px2sp(this, value)
}

fun Fragment.px2sp(value: Float): Float {
    return px2sp(context, value)
}

fun View.px2sp(value: Float): Float {
    return px2sp(context, value)
}

fun Any.px2sp(context: Context?, value: Float): Float {
    if (context == null) {
        return 0f
    }
    return (value * 10f) / context.resources.getDimension(R.dimen.sp_10)
}