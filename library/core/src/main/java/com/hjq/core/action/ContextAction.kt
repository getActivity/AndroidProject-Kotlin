package com.hjq.core.action

import android.content.Context

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/03/08
 *    desc   : Activity 相关意图
 */
interface ContextAction {

    /**
     * 获取 Context 对象
     */
    fun getContext(): Context
}