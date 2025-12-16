package com.hjq.demo.ui.activity.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.widget.ProgressBar
import com.drake.softinput.setWindowSoftInput
import com.hjq.bar.TitleBar
import com.hjq.base.ktx.createIntent
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.action.StatusAction
import com.hjq.demo.aop.CheckNet
import com.hjq.demo.aop.Log
import com.hjq.demo.app.AppActivity
import com.hjq.demo.widget.StatusLayout
import com.hjq.demo.widget.StatusLayout.OnRetryListener
import com.hjq.demo.widget.webview.BrowserChromeClient
import com.hjq.demo.widget.webview.BrowserFullScreenController
import com.hjq.demo.widget.webview.BrowserView
import com.hjq.demo.widget.webview.BrowserViewClient
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 浏览器界面
 */
class BrowserActivity : AppActivity(), StatusAction, OnRefreshListener {

    companion object {

        const val INTENT_KEY_IN_URL: String = "url"

        @CheckNet
        @Log
        fun start(context: Context, url: String) {
            if (TextUtils.isEmpty(url)) {
                return
            }
            val intent = context.createIntent(BrowserActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_URL, url)
            context.startActivity(intent)
        }
    }

    private val fullScreenController by lazy { BrowserFullScreenController() }

    private val statusLayout: StatusLayout? by lazyFindViewById(R.id.sl_browser_status)
    private val progressBar: ProgressBar? by lazyFindViewById(R.id.pb_browser_progress)
    private val refreshLayout: SmartRefreshLayout? by lazyFindViewById(R.id.sl_browser_refresh)
    private val browserView: BrowserView? by lazyFindViewById(R.id.wv_browser_view)

    override fun getLayoutId(): Int {
        return R.layout.browser_activity
    }

    override fun initView() {
        // 设置 WebView 生命管控
        browserView?.setLifecycleOwner(this)
        // 设置网页刷新监听
        refreshLayout?.setOnRefreshListener(this)

        // 解决 WebView 底部有输入框会被遮挡的问题
        setWindowSoftInput(browserView)
    }

    override fun initData() {
        showLoading()
        browserView?.apply {
            setBrowserViewClient(AppBrowserViewClient())
            setBrowserChromeClient(AppBrowserChromeClient(this))
            getString(INTENT_KEY_IN_URL)?.let { loadUrl(it) }
        }
    }

    override fun getImmersionBottomView(): View? {
        return statusLayout
    }

    override fun acquireStatusLayout(): StatusLayout? {
        return statusLayout
    }

    override fun onLeftClick(titleBar: TitleBar) {
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fullScreenController.isFullScreen()) {
                fullScreenController.exitFullScreen(this)
                return true
            }

            browserView?.apply {
                if (canGoBack()) {
                    // 后退网页并且拦截该事件
                    goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 重新加载当前页
     */
    @CheckNet
    private fun reload() {
        browserView?.reload()
    }

    /**
     * [OnRefreshListener]
     */
    override fun onRefresh(refreshLayout: RefreshLayout) {
        reload()
    }

    private inner class AppBrowserViewClient : BrowserViewClient() {

        override fun onUserRefuseSslError(handler: SslErrorHandler?) {
            super.onUserRefuseSslError(handler)
            browserView?.let {
                if (it.canGoBack()) {
                    return
                }
                // 如果当前是 WebView 的第一个页面，那么就直接销毁当前页面
                finish()
            }
        }

        override fun onWebPageLoadStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onWebPageLoadStarted(view, url, favicon)
            progressBar?.visibility = View.VISIBLE
        }

        override fun onWebPageLoadFinished(view: WebView, url: String, success: Boolean) {
            super.onWebPageLoadFinished(view, url, success)
            progressBar?.visibility = View.GONE
            refreshLayout?.finishRefresh()
            if (success) {
                showComplete()
            } else {
                showError(object : OnRetryListener {
                    override fun onRetry(layout: StatusLayout) {
                        reload()
                    }
                })
            }
        }
    }

    private inner class AppBrowserChromeClient(view: BrowserView) : BrowserChromeClient(view) {

        /**
         * 收到网页标题
         */
        override fun onReceivedTitle(view: WebView, title: String) {
            setTitle(title)
        }

        /**
         * 收到网页图标
         */
        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            setRightIcon(BitmapDrawable(resources, icon))
        }

        /**
         * 收到加载进度变化
         */
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar?.progress = newProgress
        }

        /**
         * 播放视频时进入全屏回调
         */
        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            fullScreenController.enterFullScreen(this@BrowserActivity, view, callback)
        }

        /**
         * 播放视频时退出全屏回调
         */
        override fun onHideCustomView() {
            fullScreenController.exitFullScreen(this@BrowserActivity)
        }
    }
}