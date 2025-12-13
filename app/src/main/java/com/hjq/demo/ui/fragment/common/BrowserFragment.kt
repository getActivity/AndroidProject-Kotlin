package com.hjq.demo.ui.fragment.common

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.drake.softinput.setWindowSoftInput
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.action.StatusAction
import com.hjq.demo.aop.CheckNet
import com.hjq.demo.aop.Log
import com.hjq.demo.app.AppActivity
import com.hjq.demo.app.AppFragment
import com.hjq.demo.ui.activity.common.BrowserActivity
import com.hjq.demo.widget.StatusLayout
import com.hjq.demo.widget.StatusLayout.OnRetryListener
import com.hjq.demo.widget.webview.BrowserChromeClient
import com.hjq.demo.widget.webview.BrowserView
import com.hjq.demo.widget.webview.BrowserViewClient
import com.hjq.demo.widget.webview.FullScreenModeController
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import java.util.Locale

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/10/24
 *    desc   : 浏览器 Fragment
 */
class BrowserFragment : AppFragment<AppActivity>(), StatusAction, OnRefreshListener {

    companion object {

        private const val INTENT_KEY_IN_URL: String = "url"

        @Log
        fun newInstance(url: String): BrowserFragment {
            val fragment = BrowserFragment()
            val bundle = Bundle()
            bundle.putString(INTENT_KEY_IN_URL, url)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val fullScreenModeController by lazy { FullScreenModeController() }

    private val statusLayout: StatusLayout? by lazyFindViewById(R.id.sl_browser_status)
    private val refreshLayout: SmartRefreshLayout? by lazyFindViewById(R.id.sl_browser_refresh)
    private val browserView: BrowserView? by lazyFindViewById(R.id.wv_browser_view)

    override fun getLayoutId(): Int {
        return R.layout.browser_fragment
    }

    override fun initView() {
        // 设置 WebView 生命周期回调
        browserView?.setLifecycleOwner(this)
        // 设置网页刷新监听
        refreshLayout?.setOnRefreshListener(this)

        // 解决 WebView 底部有输入框会被遮挡的问题
        setWindowSoftInput(browserView)
    }

    override fun initData() {
        browserView?.apply {
            setBrowserViewClient(AppBrowserViewClient())
            setBrowserChromeClient(AppBrowserChromeClient(this))
            loadUrl(getString(INTENT_KEY_IN_URL)!!)
        }
        showLoading()
    }

    override fun acquireStatusLayout(): StatusLayout? {
        return statusLayout
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

        override fun onWebPageLoadFinished(view: WebView, url: String?, success: Boolean) {
            super.onWebPageLoadFinished(view, url, success)
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

        /**
         * 跳转到其他链接
         */
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val scheme: String = Uri.parse(url).scheme ?: return true
            when (scheme.lowercase(Locale.getDefault())) {
                "http", "https" -> BrowserActivity.start(getAttachActivity()!!, url)
            }
            // 已经处理该链接请求
            return true
        }
    }

    private inner class AppBrowserChromeClient(view: BrowserView) : BrowserChromeClient(view) {

        /**
         * 播放视频时进入全屏回调
         */
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            fullScreenModeController.enterFullScreenMode(getAttachActivity(), view, callback)
        }

        /**
         * 播放视频时退出全屏回调
         */
        override fun onHideCustomView() {
            fullScreenModeController.exitFullScreenMode(getAttachActivity())
        }
    }
}