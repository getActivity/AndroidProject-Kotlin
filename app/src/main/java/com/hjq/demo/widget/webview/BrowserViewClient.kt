package com.hjq.demo.widget.webview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hjq.base.BaseDialog
import com.hjq.core.ktx.ANDROID_7
import com.hjq.core.ktx.createIntent
import com.hjq.demo.R
import com.hjq.demo.ui.dialog.common.MessageDialog
import timber.log.Timber

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/24
 *    desc   : 基于原生 WebViewClient 封装
 */
open class BrowserViewClient : WebViewClient() {

    private var loadingFail = false
    private var errorCode = 0
    private var description: String? = null
    private var failingUrl: String? = null

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        log(String.format("onPageStarted: url = %s", url))
        loadingFail = false
        onWebPageLoadStarted(view, url, favicon)
    }

    /**
     * 同名 API 兼容
     */
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (!request.isForMainFrame) {
            return
        }
        onReceivedError(view, error.errorCode, error.description.toString(), request.url.toString())
    }

    /**
     * 网页加载错误时回调，需要注意的是：这个方法会在 onPageFinished 之前调用
     */
    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        log(String.format("onReceivedError: errorCode = %s, description = %s, failingUrl = %s",
                            errorCode, description, failingUrl))
        this.loadingFail = true
        this.errorCode = errorCode
        this.description = description
        this.failingUrl = failingUrl
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        log(String.format("onPageFinished: url = %s", url))
        val progress = view.progress
        // 这里是为了处理网页重定向时会回调多次 onPageFinished 方法的问题
        // 会执行重定向的网站：https://xiaomi.com/ ---> https://www.mi.com/
        // 问题地址：https://stackoverflow.com/questions/3149216/how-to-listen-for-a-webview-finishing-loading-a-url
        if (progress != 100) {
            return
        }
        if (loadingFail) {
            // 加载出错之后会先调用 onReceivedError 再调用 onPageFinished
            onWebPageLoadFail(view, errorCode, description ?: "", failingUrl ?: "")
        } else {
            onWebPageLoadSuccess(view, url)
        }
        onWebPageLoadFinished(view, url, !loadingFail)
    }

    open fun onWebPageLoadStarted(view: WebView, url: String, favicon: Bitmap?) {
        log(String.format("onWebPageLoadStarted: url = %s", url))
    }

    open fun onWebPageLoadSuccess(view: WebView, url: String) {
        log(String.format("onWebPageLoadSuccess: url = %s", url))
    }

    open fun onWebPageLoadFail(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        log(String.format("onWebPageLoadFail: errorCode = %s, description = %s, failingUrl = %s",
                            errorCode, description, failingUrl))
    }

    open fun onWebPageLoadFinished(view: WebView, url: String, success: Boolean) {
        log(String.format("onWebPageLoadFinished: url = %s", url))
    }

    /**
     * 网站证书校验错误
     */
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        log(String.format("onReceivedSslError: error = %s", error.toString()))

        val context = view.context ?: return
        val errorMessage: String = when (error.primaryError) {
            SslError.SSL_NOTYETVALID -> context.getString(R.string.common_web_ssl_error_type_not_valid)
            SslError.SSL_EXPIRED -> context.getString(R.string.common_web_ssl_error_type_expired)
            SslError.SSL_IDMISMATCH -> context.getString(R.string.common_web_ssl_error_type_hostname_mismatch)
            SslError.SSL_UNTRUSTED -> context.getString(R.string.common_web_ssl_error_type_untrusted)
            SslError.SSL_DATE_INVALID -> context.getString(R.string.common_web_ssl_error_type_date_invalid)
            SslError.SSL_INVALID -> context.getString(R.string.common_web_ssl_error_type_invalid)
            else -> context.getString(R.string.common_web_ssl_error_type_other)
        }

        // 如何处理应用中的 WebView SSL 错误处理程序提醒：https://support.google.com/faqs/answer/7071387?hl=zh-Hans
        MessageDialog.Builder(context)
            .setMessage(errorMessage + "\n" + context.getString(R.string.common_web_ssl_error_inquire))
            .setConfirm(R.string.common_web_ssl_error_allow)
            .setCancel(R.string.common_web_ssl_error_reject)
            .setCancelable(false)
            .setListener(object : MessageDialog.OnListener {

                @SuppressLint("WebViewClientOnReceivedSslError")
                override fun onConfirm(dialog: BaseDialog) {
                    onUserProceedSslError(handler)
                }

                override fun onCancel(dialog: BaseDialog) {
                    onUserRefuseSslError(handler)
                }
            })
            .show()
    }

    /**
     * 用户接受了 SSL 证书错误
     */
    protected open fun onUserProceedSslError(handler: SslErrorHandler?) {
        log("onUserProceedSslError")
        handler?.proceed()
    }

    /**
     * 用户拒绝了 SSL 证书错误
     */
    protected open fun onUserRefuseSslError(handler: SslErrorHandler?) {
        log("onUserRefuseSslError")
        handler?.cancel()
    }

    /**
     * 同名 API 兼容
     */
    @TargetApi(ANDROID_7)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return shouldOverrideUrlLoading(view, request.url.toString())
    }

    /**
     * 跳转到其他链接
     */
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        log(String.format("shouldOverrideUrlLoading: url = %s", url))
        val scheme: String = Uri.parse(url).scheme ?: return false
        when (scheme) {
            "http", "https" -> view.loadUrl(url)
            "tel" -> showDialAskDialog(view, url)
        }
        return true
    }

    /**
     * 跳转到拨号界面
     */
    protected fun showDialAskDialog(view: WebView, url: String) {
        val context: Context = view.context
        MessageDialog.Builder(context)
            .setMessage(String.format(
                view.resources.getString(R.string.common_web_call_phone_title),
                url.replace("tel:", "")))
            .setConfirm(R.string.common_web_call_phone_allow)
            .setCancel(R.string.common_web_call_phone_reject)
            .setCancelable(false)
            .setListener(object : MessageDialog.OnListener {

                override fun onConfirm(dialog: BaseDialog) {
                    val intent = context.createIntent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(url)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            })
            .show()
    }

    protected fun log(message: String?) {
        if (message == null) {
            return
        }
        Timber.i(message)
    }
}