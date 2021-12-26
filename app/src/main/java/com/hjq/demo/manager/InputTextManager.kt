package com.hjq.demo.manager

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 文本输入管理类，通过管理多个 EditText 输入是否为空来启用或者禁用按钮的点击事件
 *    blog   : https://www.jianshu.com/p/fd3795e8a6b3
 */
class InputTextManager private constructor(view: View, alpha: Boolean) : TextWatcher {

    companion object {

        fun with(activity: Activity): Builder {
            return Builder(activity)
        }
    }

    /** 操作按钮的View */
    private val view: View

    /** 是否禁用后设置半透明度 */
    private val alpha: Boolean

    /** TextView集合 */
    private var viewSet: MutableList<TextView> = mutableListOf()

    /** 输入监听器 */
    private var listener: OnInputTextListener? = null

    init {
        this.view = view
        this.alpha = alpha
    }

    /**
     * 添加 TextView
     *
     * @param views     传入单个或者多个 TextView
     */
    fun addViews(views: MutableList<TextView>) {
        viewSet.addAll(views)
        for (view: TextView in views) {
            view.addTextChangedListener(this)
        }

        // 触发一次监听
        notifyChanged()
    }

    /**
     * 添加 TextView
     *
     * @param views     传入单个或者多个 TextView
     */
    fun addViews(vararg views: TextView) {
        for (view: TextView in views) {
            // 避免重复添加
            if (!viewSet.contains(view)) {
                view.addTextChangedListener(this)
                viewSet.add(view)
            }
        }
        // 触发一次监听
        notifyChanged()
    }

    /**
     * 移除 TextView 监听，避免内存泄露
     */
    fun removeViews(vararg views: TextView) {
        if (viewSet.isEmpty()) {
            return
        }
        for (view: TextView in views) {
            view.removeTextChangedListener(this)
            viewSet.remove(view)
        }
        // 触发一次监听
        notifyChanged()
    }

    /**
     * 移除所有 TextView 监听，避免内存泄露
     */
    fun removeAllViews() {
        for (view: TextView in viewSet) {
            view.removeTextChangedListener(this)
        }
        viewSet.clear()
    }

    /**
     * 设置输入监听
     */
    fun setListener(listener: OnInputTextListener?) {
        this.listener = listener
    }

    /**
     * [TextWatcher]
     */
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        notifyChanged()
    }

    /**
     * 通知更新
     */
    fun notifyChanged() {
        // 重新遍历所有的输入
        for (view: TextView in viewSet) {
            if (("" == view.text.toString())) {
                setEnabled(false)
                return
            }
        }

        listener.let {
            if (it == null) {
                setEnabled(true)
                return
            }
            setEnabled(it.onInputChange(this))
        }
    }

    /**
     * 设置 View 的事件
     *
     * @param enabled               启用或者禁用 View 的事件
     */
    fun setEnabled(enabled: Boolean) {
        if (enabled == view.isEnabled) {
            return
        }
        if (enabled) {
            //启用View的事件
            view.isEnabled = true
            if (alpha) {
                //设置不透明
                view.alpha = 1f
            }
        } else {
            //禁用View的事件
            view.isEnabled = false
            if (alpha) {
                //设置半透明
                view.alpha = 0.5f
            }
        }
    }

    class Builder constructor(private val activity: Activity) {

        /** 操作按钮的 View */
        private var view: View? = null

        /** 是否禁用后设置半透明度 */
        private var alpha: Boolean = false

        /** TextView集合 */
        private val viewSet: MutableList<TextView> = ArrayList()

        /** 输入变化监听 */
        private var listener: OnInputTextListener? = null

        fun addView(view: TextView?): Builder = apply {
            if (view != null) {
                viewSet.add(view)
            }
        }

        fun setMain(view: View): Builder = apply {
            this.view = view
        }

        fun setAlpha(alpha: Boolean): Builder = apply {
            this.alpha = alpha
        }

        fun setListener(listener: OnInputTextListener?): Builder = apply {
            this.listener = listener
        }

        fun build(): InputTextManager {
            if (view == null) {
                throw IllegalArgumentException("are you ok?")
            }
            val helper = InputTextManager(view!!, alpha)
            helper.addViews(viewSet)
            helper.setListener(listener)
            TextInputLifecycle.register(activity, helper)
            return helper
        }
    }

    private class TextInputLifecycle private constructor(
        private var activity: Activity?,
        private var textHelper: InputTextManager?
    ) : ActivityLifecycleCallbacks {

        companion object {

            fun register(activity: Activity, helper: InputTextManager?) {
                val lifecycle = TextInputLifecycle(activity, helper)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activity.registerActivityLifecycleCallbacks(lifecycle)
                } else {
                    activity.application.registerActivityLifecycleCallbacks(lifecycle)
                }
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (this.activity !== activity) {
                return
            }
            textHelper?.removeAllViews()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.activity?.unregisterActivityLifecycleCallbacks(this)
            } else {
                this.activity?.application?.unregisterActivityLifecycleCallbacks(this)
            }
            textHelper = null
            this.activity = null
        }
    }

    /**
     * 文本变化监听器
     */
    interface OnInputTextListener {

        /**
         * 输入发生了变化
         *
         * @return          返回按钮的 Enabled 状态
         */
        fun onInputChange(manager: InputTextManager?): Boolean
    }
}