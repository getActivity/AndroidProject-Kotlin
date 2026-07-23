package com.hjq.demo.app

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.demo.action.ImmersionAction
import com.hjq.demo.action.TitleBarAction

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/10/31
 *    desc   : 带标题栏的 Fragment 业务基类
 */
abstract class TitleBarFragment<A : AppActivity> : AppFragment<A>(), TitleBarAction, ImmersionAction {

    /** 标题栏对象 */
    private var titleBar: TitleBar? = null
    /** 状态栏沉浸 */
    private var immersionBar: ImmersionBar? = null
    /** 状态栏高度 LiveData  */
    private val statusBarHeightLiveData = MutableLiveData<Int?>()
    /** 导航栏高度 LiveData  */
    private val navigationBarHeightLiveData = MutableLiveData<Int?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置标题栏点击监听
        acquireTitleBar()?.setOnTitleBarListener(this)

        if (isStatusBarEnabled()) {
            // 初始化沉浸式状态栏
            getStatusBarConfig().init()
        }

        val attachActivity = getAttachActivity()
        if (attachActivity != null) {
            // 监听状态栏和导航栏高度变化
            statusBarHeightLiveData.observe(this, Observer { statusBarHeight: Int? ->
                if (statusBarHeight == null) {
                    return@Observer
                }
                getImmersionTopView()?.let {
                    it.setPadding(it.paddingLeft, statusBarHeight, it.paddingRight, it.paddingBottom)
                }
            })
            navigationBarHeightLiveData.observe(this, Observer { navigationBarHeight: Int? ->
                if (navigationBarHeight == null) {
                    return@Observer
                }
                getImmersionBottomView()?.let {
                    it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, navigationBarHeight)
                }
            })
            attachActivity.observeStatusBarHeight { statusBarHeight: Int? -> statusBarHeightLiveData.postValue(statusBarHeight) }
            attachActivity.observeNavigationBarHeight { navigationBarHeight: Int? -> navigationBarHeightLiveData.postValue(navigationBarHeight) }
        }
    }

    /**
     * 监听状态栏高度变化
     */
    fun observeStatusBarHeight(observer: Observer<Int?>) {
        observeStatusBarHeight(this, observer)
    }

    fun observeStatusBarHeight(lifecycleOwner: LifecycleOwner, observer: Observer<Int?>) {
        statusBarHeightLiveData.observe(lifecycleOwner, observer)
    }

    /**
     * 监听导航栏高度变化
     */
    fun observeNavigationBarHeight(observer: Observer<Int?>) {
        observeNavigationBarHeight(this, observer)
    }

    fun observeNavigationBarHeight(lifecycleOwner: LifecycleOwner, observer: Observer<Int?>) {
        navigationBarHeightLiveData.observe(lifecycleOwner, observer)
    }

    override fun onResume() {
        super.onResume()
        if (isStatusBarEnabled()) {
            // 重新初始化状态栏
            getStatusBarConfig().init()
        }
    }

    /**
     * 是否在 Fragment 使用沉浸式
     */
    open fun isStatusBarEnabled(): Boolean {
        return false
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    open fun getStatusBarConfig(): ImmersionBar {
        createStatusBarConfig().let {
            immersionBar = it
            return it
        }
    }

    /**
     * 初始化沉浸式
     */
    protected fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this)
            // 设置状态栏字体的颜色
            .statusBarDarkFont(isStatusBarDarkFont())
            // 设置透明的导航栏
            .transparentNavigationBar()
            // 设置导航栏图标的颜色
            .navigationBarDarkIcon(isNavigationBarDarkIcon())
    }

    /**
     * 获取状态栏字体颜色
     */
    protected open fun isStatusBarDarkFont(): Boolean {
        return getAttachActivity()?.isStatusBarDarkFont() == true
    }

    /**
     * 获取导航栏图标颜色
     */
    protected fun isNavigationBarDarkIcon(): Boolean {
        return getAttachActivity()?.isNavigationBarDarkIcon() == true
    }

    override fun acquireTitleBar(): TitleBar? {
        if (titleBar == null || !isLoading()) {
            titleBar = findTitleBar(view)
        }
        return titleBar
    }

    override fun getImmersionTopView(): View? {
        return acquireTitleBar()
    }
}