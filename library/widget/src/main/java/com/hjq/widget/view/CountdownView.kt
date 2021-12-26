package com.hjq.widget.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 验证码倒计时
 */
class CountdownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr), Runnable {

    companion object {

        /** 秒数单位文本 */
        private const val TIME_UNIT: String = "S"
    }

    /** 倒计时秒数 */
    private var totalSecond: Int = 60

    /** 当前秒数 */
    private var currentSecond: Int = 0

    /** 记录原有的文本 */
    private var recordText: CharSequence? = null

    /**
     * 设置倒计时总秒数
     */
    fun setTotalTime(totalTime: Int) {
        totalSecond = totalTime
    }

    /**
     * 开始倒计时
     */
    fun start() {
        recordText = text
        isEnabled = false
        currentSecond = totalSecond
        post(this)
    }

    /**
     * 结束倒计时
     */
    fun stop() {
        currentSecond = 0
        text = recordText
        isEnabled = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 移除延迟任务，避免内存泄露
        removeCallbacks(this)
    }

    @Suppress("SetTextI18n")
    override fun run() {
        if (currentSecond == 0) {
            stop()
            return
        }
        currentSecond--
        text = "$currentSecond $TIME_UNIT"
        postDelayed(this, 1000)
    }
}