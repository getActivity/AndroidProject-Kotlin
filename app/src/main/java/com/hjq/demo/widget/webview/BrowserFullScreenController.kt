package com.hjq.demo.widget.webview

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebChromeClient.CustomViewCallback

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2023/11/05
 *    desc   : WebView 全屏模式控制器
 */
class BrowserFullScreenController {

    /** 记录当前 Activity 方向  */
    private var recordActivityOrientation = 0

    /** 记录当前 View SystemUiVisibility 值  */
    private var recordViewSystemUiVisibility = 0

    private var customView: View? = null

    private var customViewCallback: CustomViewCallback? = null

    /**
     * 进入全屏状态
     */
    @Suppress("deprecation")
    fun enterFullScreen(activity: Activity, customView: View?, callback: CustomViewCallback?) {
        if (isFullScreen()) {
            exitFullScreen(activity)
            return
        }

        if (customView == null || callback == null) {
            return
        }

        this.customView = customView
        this.customViewCallback = callback

        val currentActivityOrientation = activity.requestedOrientation
        // 如果当前 Activity 不是横屏，就将 Activity 设置成横屏
        if (currentActivityOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            try {
                // 兼容问题：在 Android 8.0 的手机上可以固定 Activity 的方向，但是这个 Activity 不能是透明的，否则就会抛出异常
                // 复现场景：只需要给 Activity 主题设置 <item name="android:windowIsTranslucent">true</item> 属性即可
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } catch (e: IllegalStateException) {
                // java.lang.IllegalStateException: Only fullscreen activities can request orientation
                e.printStackTrace()
            }
        }
        recordActivityOrientation = currentActivityOrientation

        val contentView = getContentView(activity)
        if (contentView != null) {
            contentView.addView(customView)
            recordViewSystemUiVisibility = contentView.systemUiVisibility
            // 隐藏系统状态栏、导航栏
            contentView.systemUiVisibility = recordViewSystemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_IMMERSIVE
        }
    }

    /**
     * 退出全屏状态
     */
    @Suppress("deprecation")
    fun exitFullScreen(activity: Activity) {
        if (!isFullScreen()) {
            return
        }

        val currentActivityOrientation = activity.requestedOrientation
        // 方向和之前记录的不一样，就还原回之前的方向
        if (currentActivityOrientation != recordActivityOrientation) {
            try {
                // 兼容问题：在 Android 8.0 的手机上可以固定 Activity 的方向，但是这个 Activity 不能是透明的，否则就会抛出异常
                // 复现场景：只需要给 Activity 主题设置 <item name="android:windowIsTranslucent">true</item> 属性即可
                activity.requestedOrientation = recordActivityOrientation
            } catch (e: IllegalStateException) {
                // java.lang.IllegalStateException: Only fullscreen activities can request orientation
                e.printStackTrace()
            }
        }

        val contentView = getContentView(activity)
        if (contentView != null) {
            contentView.removeView(customView)
            // 系统样式恢复成进入全屏之前的
            contentView.systemUiVisibility = recordViewSystemUiVisibility
            // 通知 WebView 自定义 View 已从容器上移除
            customViewCallback?.onCustomViewHidden()
        }

        customView = null
        customViewCallback = null
    }

    /**
     * 判断当前是否为全屏状态
     */
    fun isFullScreen(): Boolean {
        return customView != null && customViewCallback != null
    }

    /**
     * 获取容器 View
     */
    private fun getContentView(activity: Activity): ViewGroup? {
        return activity.findViewById(Window.ID_ANDROID_CONTENT)
    }
}