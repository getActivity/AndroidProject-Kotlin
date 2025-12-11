package com.hjq.base.ktx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.hjq.base.BaseActivity
import com.hjq.base.BaseFragment
import java.util.Random
import kotlin.math.pow

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/07/02
 *    desc   : startActivity 扩展
 */

/* startActivity 简化版 */

fun Context.startActivity(block: (Intent.() -> Unit)){
    startActivity(createIntent(), block)
}

fun Context.startActivity(clazz: Class<out Activity>, block: (Intent.() -> Unit)? = null){
    startActivity(createIntent(clazz), block)
}

fun Context.startActivity(action: String, block: (Intent.() -> Unit)? = null){
    startActivity(createIntent(action), block)
}

fun Context.startActivity(intent: Intent, block: (Intent.() -> Unit)? = null){
    startActivity(intent.apply {
        block?.invoke(this)
    })
}

/* ------------------------------ 我是一条华丽的分割线 ------------------------------ */

fun Fragment.startActivity(block: (Intent.() -> Unit)){
    startActivity(createIntent(), block)
}

fun Fragment.startActivity(clazz: Class<out Activity>, block: (Intent.() -> Unit)? = null){
    startActivity(createIntent(clazz), block)
}

fun Fragment.startActivity(action: String, block: (Intent.() -> Unit)? = null){
    startActivity(createIntent(action), block)
}

fun Fragment.startActivity(intent: Intent, block: (Intent.() -> Unit)? = null){
    startActivity(intent.apply {
        block?.invoke(this)
    })
}

/* startActivityForResult 简化版 */

fun Activity.startActivityForResult(clazz: Class<out Activity>, requestCode: Int,
                                    options: Bundle? = null, block: (Intent.() -> Unit)? = null) {
    startActivityForResult(createIntent(clazz), requestCode, options, block)
}

fun Activity.startActivityForResult(action: String, requestCode: Int,
                                    options: Bundle? = null, block: (Intent.() -> Unit)? = null) {
    startActivityForResult(createIntent(action), requestCode, options, block)
}

fun Activity.startActivityForResult(intent: Intent, requestCode: Int,
                                    options: Bundle? = null, block: (Intent.() -> Unit)? = null) {
    startActivityForResult(intent.apply {
        block?.invoke(this)
    }, requestCode, options)
}

/* ------------------------------ 我是一条华丽的分割线 ------------------------------ */

fun Fragment.startActivityForResult(clazz: Class<out Activity>, requestCode: Int,
                                    options: Bundle? = null, block: (Intent.() -> Unit)? = null) {
    startActivityForResult(createIntent(clazz), requestCode, options, block)
}

fun Fragment.startActivityForResult(action: String, requestCode: Int,
                                    options: Bundle? = null, block: (Intent.() -> Unit)? = null) {
    startActivityForResult(createIntent(action), requestCode, options, block)
}

@Suppress("deprecation")
fun Fragment.startActivityForResult(intent: Intent, requestCode: Int,
                                    options: Bundle? = null, block: (Intent.() -> Unit)? = null) {
    startActivityForResult(intent.apply {
        block?.invoke(this)
    }, requestCode, options)
}

/* ------------------------------ 我是一条华丽的分割线 ------------------------------ */

fun BaseActivity.startActivityForResult(clazz: Class<out Activity>, block: (Intent.() -> Unit)? = null,
                                        callback: BaseActivity.OnActivityCallback?, options: Bundle? = null) {
    startActivityForResult(createIntent(clazz), block, callback, options)
}

fun BaseActivity.startActivityForResult(action: String, block: (Intent.() -> Unit)? = null,
                                        callback: BaseActivity.OnActivityCallback?, options: Bundle? = null) {
    startActivityForResult(createIntent(action), block, callback, options)
}

@Suppress("deprecation")
fun BaseActivity.startActivityForResult(intent: Intent, block: (Intent.() -> Unit)? = null,
                                        callback: BaseActivity.OnActivityCallback?, options: Bundle? = null) {
    // 请求码必须在 2 的 16 次方以内
    startActivityForResult(intent.apply {
        block?.invoke(this)
    }, Random().nextInt(2.0.pow(16.0).toInt()), callback, options)
}

/* ------------------------------ 我是一条华丽的分割线 ------------------------------ */

fun BaseFragment<out BaseActivity>.startActivityForResult(clazz: Class<out Activity>, block: (Intent.() -> Unit)? = null,
                                                          callback: BaseActivity.OnActivityCallback?, options: Bundle? = null) {
    getAttachActivity()?.startActivityForResult(clazz, block, callback, options)
}

fun BaseFragment<out BaseActivity>.startActivityForResult(action: String, block: (Intent.() -> Unit)? = null,
                                                          callback: BaseActivity.OnActivityCallback?, options: Bundle? = null) {
    getAttachActivity()?.startActivityForResult(action, block, callback, options)
}

@Suppress("deprecation")
fun BaseFragment<out BaseActivity>.startActivityForResult(intent: Intent, block: (Intent.() -> Unit)? = null,
                                                          callback: BaseActivity.OnActivityCallback?, options: Bundle? = null) {
    getAttachActivity()?.startActivityForResult(intent, block, callback, options)
}