package com.hjq.core.action

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import com.hjq.core.ktx.ANDROID_8
import com.hjq.core.ktx.getSdkVersion

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/01/20
 *    desc   : 修复透明 Activity 在 Android 8.0 固定屏幕出现崩溃的问题
 */
interface FixOrientationAction {

    /**
     * 是否允许 Activity 设置显示方向
     */
    fun isAllowOrientation(activity: Activity): Boolean {
        if (getSdkVersion() != ANDROID_8) {
            return true
        }
        return !isTranslucentOrFloating(activity)
    }

    /**
     * 判断 Activity 是否为半透明或者浮动
     */
    @Suppress("PrivateApi")
    fun isTranslucentOrFloating(activity: Activity): Boolean {
        var typedArray: TypedArray? = null
        try {
            val styleableRes = Class.forName($$"com.android.internal.R$styleable").getField("Window")[null] as IntArray
            typedArray = activity.obtainStyledAttributes(styleableRes)
            val method = ActivityInfo::class.java.getMethod("isTranslucentOrFloating", TypedArray::class.java)
            method.isAccessible = true
            return method.invoke(null, typedArray) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray?.recycle()
        }
        return false
    }

    /**
     * 修正在清单文件中给透明 Activity 固定方向后出现崩溃的问题，具体修复方式如下：
     * 通过反射将 screenOrientation 设置成 SCREEN_ORIENTATION_UNSPECIFIED，从而绕开系统的检查
     *
     * java.lang.IllegalStateException: Only fullscreen opaque activities can request orientation
     */
    @Suppress("JavaReflectionMemberAccess", "DiscouragedPrivateApi")
    fun fixScreenOrientation(activity: Activity) {
        try {
            val field = Activity::class.java.getDeclaredField("mActivityInfo")
            field.isAccessible = true
            val activityInfo = field[activity] as ActivityInfo
            activityInfo.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}