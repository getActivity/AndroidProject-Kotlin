package com.hjq.demo.other

import com.hjq.demo.action.ToastAction
import com.hjq.toast.ToastUtils
import com.hjq.toast.config.IToastInterceptor
import timber.log.Timber


/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/11/04
 *    desc   : 自定义 Toast 拦截器（用于追踪 Toast 调用的位置）
 */
class ToastLogInterceptor : IToastInterceptor {

    override fun intercept(text: CharSequence): Boolean {
        if (AppConfig.isLogEnable()) {
            // 获取调用的堆栈信息
            val stackTrace: Array<StackTraceElement> = Throwable().stackTrace
            // 跳过最前面两个堆栈
            var i = 2
            while (stackTrace.size > 2 && i < stackTrace.size) {

                // 获取代码行数
                val lineNumber: Int = stackTrace[i].lineNumber
                // 获取类的全路径
                val className: String = stackTrace[i].className
                if (((lineNumber <= 0) || className.startsWith(ToastUtils::class.java.name) ||
                            className.startsWith(ToastAction::class.java.name))) {
                    i++
                    continue
                }
                Timber.tag("ToastUtils")
                Timber.i("(%s:%s) %s", stackTrace[i].fileName, lineNumber, text.toString())
                break
                i++
            }
        }
        return false
    }
}