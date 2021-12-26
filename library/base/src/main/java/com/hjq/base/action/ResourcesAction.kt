package com.hjq.base.action

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/15
 *    desc   : Context 意图处理（扩展非 Context 类的方法，请不要用 Context 子类实现此接口）
 */
interface ResourcesAction {

    fun getContext(): Context

    fun getResources(): Resources {
        return getContext().resources
    }

    fun getString(@StringRes id: Int): String? {
        return getContext().getString(id)
    }

    fun getString(@StringRes id: Int, vararg formatArgs: Any?): String? {
        return getResources().getString(id, *formatArgs)
    }

    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(getContext(), id)
    }

    @ColorInt
    fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(getContext(), id)
    }

    fun <S> getSystemService(serviceClass: Class<S>): S {
        return ContextCompat.getSystemService(getContext(), serviceClass)!!
    }
}