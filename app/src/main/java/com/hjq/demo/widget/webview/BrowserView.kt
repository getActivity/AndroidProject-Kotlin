package com.hjq.demo.widget.webview

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hjq.base.action.ContextAction
import com.hjq.demo.other.AppConfig
import com.hjq.nested.scroll.layout.NestedScrollWebView
import timber.log.Timber

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/24
 *    desc   : 基于原生 WebView 封装
 */
@Suppress("SetJavaScriptEnabled")
class BrowserView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.webViewStyle, defStyleRes: Int = 0) :
    NestedScrollWebView(context, attrs, defStyleAttr, defStyleRes),
    LifecycleEventObserver, ContextAction {

    companion object {

        init {
            // WebView 调试模式开关
            setWebContentsDebuggingEnabled(AppConfig.isDebug())
        }
    }

    init {
        val settings: WebSettings = settings
        // 允许文件访问
        settings.allowFileAccess = true
        // 允许网页定位
        settings.setGeolocationEnabled(true)
        // 允许保存密码
        //settings.setSavePassword(true);
        // 开启 JavaScript
        settings.javaScriptEnabled = true
        // 允许网页弹对话框
        settings.javaScriptCanOpenWindowsAutomatically = true
        // 加快网页加载完成的速度，等页面完成再加载图片
        settings.loadsImagesAutomatically = true
        // 本地 DOM 存储（解决加载某些网页出现白板现象）
        settings.domStorageEnabled = true
        // 解决 Android 5.0 上 WebView 默认不允许加载 Http 与 Https 混合内容
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // 不显示滚动条
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    override fun loadUrl(url: String) {
        super.loadUrl(url)
        log(String.format("loadUrl: url = %s", url))
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String?, String?>) {
        super.loadUrl(url, additionalHttpHeaders)
        log(String.format("loadUrl: url = %s, additionalHttpHeaders = %s", url, additionalHttpHeaders))
    }

    override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?,
                                    encoding: String?, historyUrl: String?) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
        log(String.format("loadUrl: baseUrl = %s, mimeType = %s, encoding = %s, historyUrl = %s",
                baseUrl, mimeType, encoding, historyUrl))
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        super.loadData(data, mimeType, encoding)
        log(String.format("loadUrl: mimeType = %s, encoding = %s", mimeType, encoding))
    }

    override fun postUrl(url: String, postData: ByteArray) {
        super.postUrl(url, postData)
        log(String.format("postUrl: url = %s", url))
    }

    override fun reload() {
        super.reload()
        log(String.format("reload: url = %s", url))
    }

    /**
     * 获取当前的 url
     *
     * @return      返回原始的 url，因为有些 url 是被 WebView 解码过的
     */
    override fun getUrl(): String? {
        // 避免开始时同时加载两个地址而导致的崩溃
        return super.getOriginalUrl() ?: return super.getUrl()
    }

    /**
     * 设置 WebView 生命管控（自动回调生命周期方法）
     */
    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    /**
     * [LifecycleEventObserver]
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_STOP -> onPause()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> {}
        }
    }

    /**
     * 销毁 WebView
     */
    fun onDestroy() {
        log("onDestroy")
        // 停止加载网页
        stopLoading()
        // 清除历史记录
        clearHistory()
        // 取消监听引用
        setBrowserChromeClient(null)
        setBrowserViewClient(null)
        // 移除WebView所有的View对象
        removeAllViews()
        // 销毁此的WebView的内部状态
        destroy()
    }

    /**
     * 已过时
     */
    @Deprecated("请使用 {@link #setBrowserViewClient(BrowserViewClient)}", ReplaceWith(
        "super.setWebViewClient(client)",
        "com.hjq.nested.scroll.layout.NestedScrollWebView"))
    override fun setWebViewClient(client: WebViewClient) {
        super.setWebViewClient(client)
    }

    fun setBrowserViewClient(client: BrowserViewClient?) {
        if (client == null) {
            super.setWebViewClient(WebViewClient())
            return
        }
        super.setWebViewClient(client)
    }

    /**
     * 已过时
     */
    @Deprecated("请使用 {@link #setBrowserChromeClient(BrowserChromeClient)}", ReplaceWith(
        "super.setWebChromeClient(client)",
        "com.hjq.nested.scroll.layout.NestedScrollWebView"))
    override fun setWebChromeClient(client: WebChromeClient?) {
        super.setWebChromeClient(client)
    }

    fun setBrowserChromeClient(client: BrowserChromeClient?) {
        super.setWebChromeClient(client)
    }

    protected fun log(message: String?) {
        Timber.i(message)
    }
}