package com.hjq.demo.other

import com.hjq.toast.ToastLogInterceptor
import timber.log.Timber

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/11/04
 *    desc   : 自定义 Toast 拦截器（用于追踪 Toast 调用的位置）
 */
class ToastInterceptor : ToastLogInterceptor() {

    override fun isLogEnable(): Boolean {
        return AppConfig.isLogEnable()
    }

    override fun printLog(msg: String?) {
        Timber.tag("Toaster")
        Timber.i(msg)
    }
}