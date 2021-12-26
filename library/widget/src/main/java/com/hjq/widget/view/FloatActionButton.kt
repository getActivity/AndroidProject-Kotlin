package com.hjq.widget.view

import android.animation.ValueAnimator
import android.content.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/09/17
 *    desc   : 带悬浮动画的按钮
 */
class FloatActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {

        /** 动画显示时长 */
        private const val ANIM_TIME: Int = 300
    }

    /**
     * 显示
     */
    fun show() {
        removeCallbacks(hideRunnable)
        postDelayed(showRunnable, (ANIM_TIME * 2).toLong())
    }

    /**
     * 隐藏
     */
    fun hide() {
        removeCallbacks(showRunnable)
        post(hideRunnable)
    }

    /**
     * 显示悬浮球动画
     */
    private val showRunnable: Runnable = Runnable {
        if (visibility == INVISIBLE) {
            visibility = VISIBLE
        }
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = ANIM_TIME.toLong()
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            val value: Float = animation.animatedValue as Float
            alpha = value
            scaleX = value
            scaleY = value
        }
        valueAnimator.start()
    }

    /**
     * 隐藏悬浮球动画
     */
    private val hideRunnable: Runnable = Runnable {
        if (visibility == INVISIBLE) {
            return@Runnable
        }
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)
        valueAnimator.duration = ANIM_TIME.toLong()
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            val value: Float = animation.animatedValue as Float
            alpha = value
            scaleX = value
            scaleY = value
            if (value == 0f) {
                visibility = INVISIBLE
            }
        }
        valueAnimator.start()
    }
}