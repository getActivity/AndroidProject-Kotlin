package com.hjq.demo.app

import android.content.Intent
import android.view.View
import android.view.WindowInsets
import androidx.annotation.StringRes
import com.gyf.immersionbar.ImmersionBar
import com.hjq.bar.TitleBar
import com.hjq.base.BaseActivity
import com.hjq.demo.R
import com.hjq.demo.action.ImmersionAction
import com.hjq.demo.action.TitleBarAction
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.ktx.isAndroid16
import com.hjq.demo.ktx.toast
import com.hjq.demo.ui.dialog.common.WaitDialog
import com.hjq.http.config.IRequestApi
import com.hjq.http.listener.OnHttpListener
import com.hjq.umeng.UmengClient.onActivityResult

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : Activity 业务基类
 */
abstract class AppActivity : BaseActivity(), TitleBarAction, ImmersionAction, OnHttpListener<Any> {

    private var titleBar: TitleBar? = null

    /** 状态栏沉浸 */
    private var immersionBar: ImmersionBar? = null

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
        if ((dialogCount != 0) || (dialog == null) || !dialog!!.isShowing()) {
            return
        }
        dialog?.dismiss()
    }

    override fun initLayout() {
        super.initLayout()

        val titleBar = getTitleBar()
        titleBar?.setOnTitleBarListener(this)

        // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            getStatusBarConfig().init()
        }

        // 适配 Android 15 EdgeToEdge 特性，这里你可能好奇为什么判断的是 Android 16？
        // 因为我在主题样式中注册了一个 windowOptOutEdgeToEdgeEnforcement 属性，
        // 代表跳过在 Android 15 的 EdgeToEdge 特性适配，但到了 Android 16 上面就失效了。
        if (isAndroid16()) {
            window.decorView.setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
                override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
                    val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
                    val immersionTopView = getImmersionTopView()
                    val immersionBottomView = getImmersionBottomView()
                    if (immersionTopView != null && immersionTopView === immersionBottomView) {
                        immersionTopView.setPadding(immersionTopView.getPaddingLeft(), systemBars.top,
                            immersionTopView.getPaddingRight(), systemBars.bottom
                        )
                        return insets
                    }
                    immersionTopView?.setPadding(immersionTopView.getPaddingLeft(), systemBars.top,
                        immersionTopView.getPaddingRight(),
                        immersionTopView.paddingBottom
                    )
                    immersionBottomView?.setPadding(
                        immersionBottomView.getPaddingLeft(),
                        immersionBottomView.paddingTop,
                        immersionBottomView.getPaddingRight(),
                        systemBars.bottom
                    )
                    return insets
                }
            })
        } else {
            val immersionTopView = getImmersionTopView()
            if (immersionTopView != null) {
                ImmersionBar.setTitleBar(this, immersionTopView)
            }
        }
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
        return true
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    open fun getStatusBarConfig(): ImmersionBar {
        if (immersionBar == null) {
            immersionBar = createStatusBarConfig()
        }
        return immersionBar!!
    }

    /**
     * 初始化沉浸式状态栏
     */
    protected open fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this)
            // 默认状态栏字体颜色为黑色
            .statusBarDarkFont(isStatusBarDarkFont())
            // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
            // 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
            .autoDarkModeEnable(true, 0.2f)
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
        getTitleBar()?.title = title
    }

    override fun getTitleBar(): TitleBar? {
        if (titleBar == null) {
            titleBar = obtainTitleBar(getContentView())
        }
        return titleBar
    }

    override fun getImmersionTopView(): View? {
        return getTitleBar()
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