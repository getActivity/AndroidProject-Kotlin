package com.hjq.demo.app

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.base.BaseActivity
import com.hjq.demo.R
import com.hjq.demo.action.ImmersionAction
import com.hjq.demo.action.TitleBarAction
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.ktx.toast
import com.hjq.demo.ui.dialog.common.WaitDialog
import com.hjq.http.config.IRequestApi
import com.hjq.http.listener.OnHttpListener
import com.hjq.umeng.sdk.UmengClient.onActivityResult

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : Activity 业务基类
 */
abstract class AppActivity : BaseActivity(), TitleBarAction, ImmersionAction, OnHttpListener<Any> {

    /** 标题栏对象 */
    private var titleBar: TitleBar? = null
    /** 状态栏沉浸 */
    private var immersionBar: ImmersionBar? = null
    /** 状态栏高度 LiveData  */
    private val statusBarHeightLiveData = MutableLiveData<Int?>()
    /** 导航栏高度 LiveData  */
    private val navigationBarHeightLiveData = MutableLiveData<Int?>()

    /** 加载对话框 */
    private var dialog: WaitDialog.Builder? = null

    /** 对话框数量 */
    private var dialogCount: Int = 0

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        dialog.let {
            return it != null && it.isShowing()
        }
    }

    open fun showLoadingDialog() {
        showLoadingDialog(getString(R.string.common_loading))
    }

    /**
     * 显示加载对话框
     */
    open fun showLoadingDialog(message: String) {
        if (isFinishing || isDestroyed) {
            return
        }
        dialogCount++
        postDelayed(Runnable {
            if ((dialogCount <= 0) || isFinishing || isDestroyed) {
                return@Runnable
            }
            if (dialog == null) {
                dialog = WaitDialog.Builder(this)
                    .setCancelable(false)
            }
            dialog?.let {
                it.setMessage(message)
                if (!it.isShowing()) {
                    it.show()
                }
            }
        }, 300)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideLoadingDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (dialogCount > 0) {
            dialogCount--
        }
        if (dialogCount != 0) {
            return
        }
        dialog?.let {
            if (it.isShowing()) {
                it.dismiss()
            }
        }
    }

    override fun initLayout() {
        super.initLayout()

        acquireTitleBar()?.setOnTitleBarListener(this)

        // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            getStatusBarConfig().init()
        }

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
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val windowInsets: Insets = getWindowInsets(insets)
            val statusBarHeight = statusBarHeightLiveData.getValue()
            if (statusBarHeight == null || statusBarHeight != windowInsets.top) {
                statusBarHeightLiveData.postValue(windowInsets.top)
            }
            val navigationBarHeight = navigationBarHeightLiveData.getValue()
            if (navigationBarHeight == null || navigationBarHeight != windowInsets.bottom) {
                navigationBarHeightLiveData.postValue(windowInsets.bottom)
            }
            insets
        }
    }

    /**
     * 获取系统栏的高度
     */
    fun getWindowInsets(insets: WindowInsetsCompat): Insets {
        return insets.getInsets(WindowInsetsCompat.Type.systemBars())
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

    /**
     * 是否使用沉浸式状态栏
     */
    protected open fun isStatusBarEnabled(): Boolean {
        return true
    }

    /**
     * 状态栏字体深色模式
     */
    open fun isStatusBarDarkFont(): Boolean {
        // 返回 true 表示黑色字体
        return true
    }

    /**
     * 获取导航栏图标颜色
     */
    open fun isNavigationBarDarkIcon(): Boolean {
        // 返回 true 表示黑色图标
        return true
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
     * 初始化沉浸式状态栏
     */
    protected open fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this)
            // 设置状态栏字体的颜色
            .statusBarDarkFont(isStatusBarDarkFont())
            // 设置透明的导航栏
            .transparentNavigationBar()
            // 设置导航栏图标的颜色
            .navigationBarDarkIcon(isNavigationBarDarkIcon())
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(@StringRes id: Int) {
        title = getString(id)
    }

    /**
     * 设置标题栏的标题
     */
    override fun setTitle(title: CharSequence?) {
        super<BaseActivity>.setTitle(title)
        acquireTitleBar()?.title = title
    }

    override fun acquireTitleBar(): TitleBar? {
        if (titleBar == null) {
            titleBar = findTitleBar(getContentView())
        }
        return titleBar
    }

    override fun getImmersionTopView(): View? {
        return acquireTitleBar()
    }

    override fun onLeftClick(titleBar: TitleBar) {
        onBackPressed()
    }

    /**
     * [OnHttpListener]
     */
    override fun onHttpStart(api: IRequestApi) {
        showLoadingDialog()
    }

    override fun onHttpSuccess(result: Any) {
        if (result is HttpData<*>) {
            toast(result.getMessage())
        }
    }

    override fun onHttpFail(throwable: Throwable) {
        toast(throwable.message)
    }

    override fun onHttpEnd(api: IRequestApi) {
        hideLoadingDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isShowDialog()) {
            hideLoadingDialog()
        }
        dialog = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 友盟回调
        onActivityResult(this, requestCode, resultCode, data)
    }
}