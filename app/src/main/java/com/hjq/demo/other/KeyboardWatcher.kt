package com.hjq.demo.other

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.graphics.*
import android.os.*
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/07/04
 *    desc   : 软键盘监听类
 */
class KeyboardWatcher private constructor(private var activity: Activity) :
    OnGlobalLayoutListener, ActivityLifecycleCallbacks {

    companion object {

        fun with(activity: Activity): KeyboardWatcher {
            return KeyboardWatcher(activity)
        }
    }

    private var contentView: View = activity.findViewById(Window.ID_ANDROID_CONTENT)
    private var listeners: SoftKeyboardStateListener? = null
    private var softKeyboardOpened: Boolean = false
    private var statusBarHeight: Int = 0

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(this)
        } else {
            activity.application.registerActivityLifecycleCallbacks(this)
        }
        contentView.viewTreeObserver.addOnGlobalLayoutListener(this)

        // 获取 status_bar_height 资源的 ID
        val resourceId: Int = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源 ID 获取响应的尺寸值
            statusBarHeight = activity.resources.getDimensionPixelSize(resourceId)
        }
    }

    /**
     * [ViewTreeObserver.OnGlobalLayoutListener]
     */
    override fun onGlobalLayout() {
        val r = Rect()
        //r will be populated with the coordinates of your view that area still visible.
        contentView.getWindowVisibleDisplayFrame(r)
        val heightDiff: Int = contentView.rootView.height - (r.bottom - r.top)
        if (!softKeyboardOpened && heightDiff > contentView.rootView.height / 4) {
            softKeyboardOpened = true
            if ((activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                listeners?.onSoftKeyboardOpened(heightDiff - statusBarHeight)
            } else {
                listeners?.onSoftKeyboardOpened(heightDiff)
            }
        } else if (softKeyboardOpened && heightDiff < contentView.rootView.height / 4) {
            softKeyboardOpened = false
            listeners?.onSoftKeyboardClosed()
        }
    }

    /**
     * 设置软键盘弹出监听
     */
    fun setListener(listener: SoftKeyboardStateListener?) {
        listeners = listener
    }

    /**
     * [ActivityLifecycleCallbacks]
     */

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (this.activity === activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.activity.unregisterActivityLifecycleCallbacks(this)
            } else {
                this.activity.application.unregisterActivityLifecycleCallbacks(this)
            }
            contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            listeners = null
        }
    }

    /**
     * 软键盘状态监听器
     */
    interface SoftKeyboardStateListener {

        /**
         * 软键盘弹出了
         *
         * @param keyboardHeight            软键盘高度
         */
        fun onSoftKeyboardOpened(keyboardHeight: Int)

        /**
         * 软键盘收起了
         */
        fun onSoftKeyboardClosed()
    }
}