package com.hjq.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.hjq.base.action.BundleAction
import com.hjq.base.action.ClickAction
import com.hjq.base.action.HandlerAction

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : Fragment 技术基类
 */
abstract class BaseFragment<A : BaseActivity> : Fragment(),
    Application.ActivityLifecycleCallbacks,
    HandlerAction, ClickAction, BundleAction {

    /** Activity 对象 */
    private var activity: A? = null

    /** 根布局 */
    private var rootView: View? = null

    /** 当前是否加载过 */
    private var loading: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 获得全局的 Activity
        activity = requireActivity() as A
        registerAttachActivityLifecycle()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (getLayoutId() <= 0) {
            return null
        }
        loading = false
        rootView = inflater.inflate(getLayoutId(), container, false)
        initView()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (loading) {
            return
        }

        loading = true
        initData()
    }

    /**
     * Activity 获取焦点回调
     */
    protected open fun onActivityStart(attachActivity: A) {
        // default implementation ignored
    }

    /**
     * Activity 可见回调
     */
    protected open fun onActivityResume(attachActivity: A) {
        // default implementation ignored
    }

    /**
     * Activity 不可见回调
     */
    protected open fun onActivityPause(attachActivity: A) {
        // default implementation ignored
    }

    /**
     * Activity 失去焦点回调
     */
    protected open fun onActivityStop(attachActivity: A) {
        // default implementation ignored
    }

    /**
     * Activity 销毁时回调
     */
    protected open fun onActivityDestroy(attachActivity: A) {
        // default implementation ignored
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }

    override fun onDestroy() {
        removeCallbacks()
        super.onDestroy()
        loading = false
    }

    override fun onDetach() {
        super.onDetach()
        unregisterAttachActivityLifecycle()
        activity = null
    }

    /**
     * 这个 Fragment 是否已经加载过了
     */
    open fun isLoading(): Boolean {
        return loading
    }

    override fun getView(): View? {
        return rootView
    }

    /**
     * 获取绑定的 Activity，防止出现 getActivity 为空
     */
    open fun getAttachActivity(): A? {
        return activity
    }

    /**
     * 获取 Application 对象
     */
    open fun getApplication(): Application? {
        activity?.let { return it.application }
        return null
    }

    /**
     * 获取布局 ID
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 初始化控件
     */
    protected abstract fun initView()

    /**
     * 初始化数据
     */
    protected abstract fun initData()

    /**
     * 根据资源 id 获取一个 View 对象
     */
    override fun <V : View?> findViewById(@IdRes id: Int): V? {
        return rootView?.findViewById(id)
    }

    override fun getBundle(): Bundle? {
        return arguments
    }

    /**
     * 销毁当前 Fragment 所在的 Activity
     */
    open fun finish() {
        this.activity?.let {
            if (it.isFinishing || it.isDestroyed) {
                return
            }
            it.finish()
        }
    }

    /**
     * Fragment 按键事件派发
     */
    open fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val fragments: MutableList<Fragment?> = childFragmentManager.fragments
        for (fragment: Fragment? in fragments) {
            // 这个子 Fragment 必须是 BaseFragment 的子类，并且处于可见状态
            if (fragment !is BaseFragment<*> || fragment.lifecycle.currentState != Lifecycle.State.RESUMED) {
                continue
            }
            // 将按键事件派发给子 Fragment 进行处理
            if (fragment.dispatchKeyEvent(event)) {
                // 如果子 Fragment 拦截了这个事件，那么就不交给父 Fragment 处理
                return true
            }
        }
        return when (event.action) {
            KeyEvent.ACTION_DOWN -> onKeyDown(event.keyCode, event)
            KeyEvent.ACTION_UP -> onKeyUp(event.keyCode, event)
            else -> false
        }
    }

    /**
     * 按键按下事件回调
     */
    open fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // 默认不拦截按键事件
        return false
    }

    /**
     * 按键抬起事件回调
     */
    open fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        // 默认不拦截按键事件
        return false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // default implementation ignored
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity !== this.activity) {
            return
        }
        onActivityStart(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity !== this.activity) {
            return
        }
        onActivityResume(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity !== this.activity) {
            return
        }
        onActivityPause(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity !== this.activity) {
            return
        }
        onActivityStop(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // default implementation ignored
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity !== this.activity) {
            return
        }
        onActivityDestroy(activity)
        unregisterAttachActivityLifecycle()
    }

    /**
     * 注册绑定 Activity 生命周期回调
     */
    private fun registerAttachActivityLifecycle() {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.registerActivityLifecycleCallbacks(this)
            } else {
                it.application.registerActivityLifecycleCallbacks(this)
            }
        }
    }

    /**
     * 反注册绑定 Activity 生命周期回调
     */
    private fun unregisterAttachActivityLifecycle() {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.unregisterActivityLifecycleCallbacks(this)
            } else {
                it.application.unregisterActivityLifecycleCallbacks(this)
            }
        }
    }
}