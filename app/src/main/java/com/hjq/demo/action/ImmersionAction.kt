package com.hjq.demo.action

import android.view.View
import com.hjq.bar.OnTitleBarListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2025/12/13
 *    desc   : 沉浸式意图
 */
interface ImmersionAction : OnTitleBarListener {

    /**
     * 获取需要沉浸的顶部 View 对象
     */
    fun getImmersionTopView(): View? {
        return null;
    }

    /**
     * 获取需要沉浸的底部 View 对象
     */
    fun getImmersionBottomView(): View? {
        return null;
    }
}