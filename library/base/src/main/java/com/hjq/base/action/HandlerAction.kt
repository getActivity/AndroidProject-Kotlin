package com.hjq.base.action

import android.os.Handler
import android.os.Looper
import android.os.SystemClock

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/15
 *    desc   : Handler 意图处理
 */
interface HandlerAction {

    companion object {
        val HANDLER: Handler = Handler(Looper.getMainLooper())
    }

    /**
     * 获取 Handler
     */
    fun getHandler(): Handler {
        return HANDLER
    }

    /**
     * 延迟执行
     */
    fun post(runnable: Runnable): Boolean {
        return postDelayed(runnable, 0)
    }

    /**
     * 延迟一段时间执行
     */
    fun postDelayed(runnable: Runnable, delayMillis: Long): Boolean {
        return postAtTime(runnable, SystemClock.uptimeMillis() + if (delayMillis < 0) 0 else delayMillis)
    }

    /**
     * 在指定的时间执行
     */
    fun postAtTime(runnable: Runnable, uptimeMillis: Long): Boolean {
        // 发送和当前对象相关的消息回调
        return HANDLER.postAtTime(runnable, this, uptimeMillis)
    }

    /**
     * 移除单个消息回调
     */
    fun removeCallbacks(runnable: Runnable) {
        HANDLER.removeCallbacks(runnable)
    }

    /**
     * 移除全部消息回调
     */
    fun removeCallbacks() {
        // 移除和当前对象相关的消息回调
        HANDLER.removeCallbacksAndMessages(this)
    }
}