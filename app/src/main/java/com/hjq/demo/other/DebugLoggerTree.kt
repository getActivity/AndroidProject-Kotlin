package com.hjq.demo.other

import com.hjq.core.ktx.isAndroid8
import timber.log.Timber.DebugTree

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/08/12
 *    desc   : 自定义日志打印规则
 */
class DebugLoggerTree : DebugTree() {

    companion object {
        private const val MAX_TAG_LENGTH: Int = 23
    }

    /**
     * 创建日志堆栈 TAG
     */
    override fun createStackElementTag(element: StackTraceElement): String {
        val tag = "(" + element.fileName + ":" + element.lineNumber + ")"
        // 日志 TAG 长度限制已经在 Android 8.0 被移除
        return if (tag.length <= MAX_TAG_LENGTH || isAndroid8()) {
            tag
        } else
            tag.take(MAX_TAG_LENGTH)
    }
}