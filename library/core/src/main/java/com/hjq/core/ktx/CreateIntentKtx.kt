package com.hjq.core.ktx

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/07/02
 *    desc   : Intent 扩展
 */

/* 创建一个 Intent 对象 */

fun Context.createIntent() : Intent {
    val intent = Intent()
    handlerIntentFlag(intent)
    return intent
}

fun Context.createIntent(clazz: Class<out Any>) : Intent {
    val intent = Intent(this, clazz)
    handlerIntentFlag(intent)
    return intent
}
fun Context.createIntent(action: String) : Intent {
    val intent = Intent(action)
    handlerIntentFlag(intent)
    return intent
}

fun Fragment.createIntent() : Intent {
    val intent = Intent()
    this.context?.handlerIntentFlag(intent)
    return intent
}

fun Fragment.createIntent(clazz: Class<out Any>) : Intent {
    val intent = Intent(this.context, clazz)
    this.context?.handlerIntentFlag(intent)
    return intent
}

fun Fragment.createIntent(action: String) : Intent {
    val intent = Intent(action)
    this.context?.handlerIntentFlag(intent)
    return intent
}

/* 创建一个选择器 Intent 对象 */

fun Context.createChooserIntent(intent: Intent, title: CharSequence? = "") : Intent {
    val chooserIntent = Intent.createChooser(intent, title)
    handlerIntentFlag(chooserIntent)
    return chooserIntent
}

fun Fragment.createChooserIntent(intent: Intent, title: CharSequence? = "") : Intent {
    val chooserIntent = Intent.createChooser(intent, title)
    this.context?.handlerIntentFlag(chooserIntent)
    return chooserIntent
}

fun Context.handlerIntentFlag(intent: Intent) {
    if (this is Activity) {
        return
    }
    // 如果当前的上下文不是 Activity，调用 startActivity 必须加入新任务栈的标记，否则会报错：android.util.AndroidRuntimeException
    // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}