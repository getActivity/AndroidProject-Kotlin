package com.hjq.demo.widget

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.webkit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hjq.base.BaseActivity
import com.hjq.base.BaseActivity.OnActivityCallback
import com.hjq.base.BaseDialog
import com.hjq.base.action.ActivityAction
import com.hjq.demo.R
import com.hjq.demo.other.AppConfig
import com.hjq.demo.other.PermissionCallback
import com.hjq.demo.ui.activity.ImageSelectActivity
import com.hjq.demo.ui.activity.ImageSelectActivity.OnPhotoSelectListener
import com.hjq.demo.ui.activity.VideoSelectActivity
import com.hjq.demo.ui.activity.VideoSelectActivity.OnVideoSelectListener
import com.hjq.demo.ui.activity.VideoSelectActivity.VideoBean
import com.hjq.demo.ui.dialog.InputDialog
import com.hjq.demo.ui.dialog.MessageDialog
import com.hjq.demo.ui.dialog.TipsDialog
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.widget.layout.NestedScrollWebView
import timber.log.Timber
import java.io.File
import java.util.*

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
    LifecycleEventObserver, ActivityAction {

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 解决 Android 5.0 上 WebView 默认不允许加载 Http 与 Https 混合内容
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // 不显示滚动条
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    /**
     * 获取当前的 url
     *
     * @return      返回原始的 url，因为有些url是被WebView解码过的
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
        }
    }

    /**
     * 销毁 WebView
     */
    fun onDestroy() {
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
    @Deprecated("请使用 {@link BrowserViewClient}", ReplaceWith(
        "super.setWebViewClient(client)",
        "com.hjq.widget.layout.NestedScrollWebView"))
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
    @Deprecated("请使用 {@link BrowserChromeClient}", ReplaceWith(
        "super.setWebChromeClient(client)",
        "com.hjq.widget.layout.NestedScrollWebView"))
    override fun setWebChromeClient(client: WebChromeClient?) {
        super.setWebChromeClient(client)
    }

    fun setBrowserChromeClient(client: BrowserChromeClient?) {
        super.setWebChromeClient(client)
    }

    open class BrowserViewClient : WebViewClient() {

        /**
         * 网站证书校验错误
         */
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            // 如何处理应用中的 WebView SSL 错误处理程序提醒：https://support.google.com/faqs/answer/7071387?hl=zh-Hans
            MessageDialog.Builder(view.context)
                .setMessage(R.string.common_web_ssl_error_title)
                .setConfirm(R.string.common_web_ssl_error_allow)
                .setCancel(R.string.common_web_ssl_error_reject)
                .setCancelable(false)
                .setListener(object : MessageDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog?) {
                        handler.proceed()
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                        handler.cancel()
                    }
                })
                .show()
        }

        /**
         * 同名 API 兼容
         */
        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            if (request.isForMainFrame) {
                onReceivedError(view, error.errorCode, error.description.toString(), request.url.toString())
            }
        }

        /**
         * 加载错误
         */
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            super.onReceivedError(view, errorCode, description, failingUrl)
        }

        /**
         * 同名 API 兼容
         */
        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return shouldOverrideUrlLoading(view, request.url.toString())
        }

        /**
         * 跳转到其他链接
         */
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Timber.i("WebView shouldOverrideUrlLoading：%s", url)
            val scheme: String = Uri.parse(url).scheme ?: return false
            when (scheme) {
                "http", "https" -> view.loadUrl(url)
                "tel" -> dialing(view, url)
            }
            return true
        }

        /**
         * 跳转到拨号界面
         */
        protected fun dialing(view: WebView, url: String) {
            val context: Context = view.context
            MessageDialog.Builder(context)
                .setMessage(String.format(
                    view.resources.getString(R.string.common_web_call_phone_title),
                    url.replace("tel:", "")))
                .setConfirm(R.string.common_web_call_phone_allow)
                .setCancel(R.string.common_web_call_phone_reject)
                .setCancelable(false)
                .setListener(object : MessageDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog?) {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse(url)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                })
                .show()
        }
    }

    open class BrowserChromeClient constructor(private val webView: BrowserView) : WebChromeClient() {

        /**
         * 网页弹出警告框
         */
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            val activity: Activity = webView.getActivity() ?: return false
            TipsDialog.Builder(activity)
                .setIcon(TipsDialog.ICON_WARNING)
                .setMessage(message)
                .setCancelable(false)
                .addOnDismissListener(object : BaseDialog.OnDismissListener {

                    override fun onDismiss(dialog: BaseDialog?) {
                        result.confirm()
                    }
                })
                .show()
            return true
        }

        /**
         * 网页弹出确定取消框
         */
        override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
            val activity: Activity = webView.getActivity() ?: return false
            MessageDialog.Builder(activity)
                .setMessage(message)
                .setCancelable(false)
                .setListener(object : MessageDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog?) {
                        result.confirm()
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                        result.cancel()
                    }
                })
                .show()
            return true
        }

        /**
         * 网页弹出输入框
         */
        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            val activity: Activity = webView.getActivity() ?: return false
            InputDialog.Builder(activity)
                .setContent(defaultValue)
                .setHint(message)
                .setCancelable(false)
                .setListener(object : InputDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog?, content: String) {
                        result.confirm(content)
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                        result.cancel()
                    }
                })
                .show()
            return true
        }

        /**
         * 网页请求定位功能
         * 测试地址：https://map.baidu.com/
         */
        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            val activity: Activity = webView.getActivity() ?: return
            MessageDialog.Builder(activity)
                .setMessage(R.string.common_web_location_permission_title)
                .setConfirm(R.string.common_web_location_permission_allow)
                .setCancel(R.string.common_web_location_permission_reject)
                .setCancelable(false)
                .setListener(object : MessageDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog?) {
                        XXPermissions.with(activity)
                            .permission(Permission.ACCESS_FINE_LOCATION)
                            .permission(Permission.ACCESS_COARSE_LOCATION)
                            .request(object : PermissionCallback() {
                                override fun onGranted(
                                    permissions: MutableList<String?>?,
                                    all: Boolean
                                ) {
                                    if (all) {
                                        callback.invoke(origin, true, true)
                                    }
                                }
                            })
                    }

                    override fun onCancel(dialog: BaseDialog?) {
                        callback.invoke(origin, false, true)
                    }
                })
                .show()
        }

        /**
         * 网页弹出选择文件请求
         * 测试地址：https://app.xunjiepdf.com/jpg2pdf/、http://www.script-tutorials.com/demos/199/index.html
         *
         * @param callback              文件选择回调
         * @param params                文件选择参数
         */
        override fun onShowFileChooser(webView: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
            val activity: Activity? = this.webView.getActivity()
            if (activity !is BaseActivity) {
                return false
            }
            XXPermissions.with(activity)
                .permission(*Permission.Group.STORAGE)
                .request(object : PermissionCallback() {
                    override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                        if (all) {
                            openSystemFileChooser(activity, params, callback)
                        }
                    }

                    override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                        super.onDenied(permissions, never)
                        callback.onReceiveValue(null)
                    }
                })
            return true
        }

        /**
         * 打开系统文件选择器
         */
        private fun openSystemFileChooser(activity: BaseActivity, params: FileChooserParams, callback: ValueCallback<Array<Uri>>) {
            val intent: Intent = params.createIntent()
            val mimeTypes: Array<String>? = params.acceptTypes
            val multipleSelect: Boolean = params.mode == FileChooserParams.MODE_OPEN_MULTIPLE
            if ((mimeTypes != null) && (mimeTypes.isNotEmpty()) && !TextUtils.isEmpty(mimeTypes[0])) {
                // 要过滤的文件类型
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                if (mimeTypes.size == 1) {
                    when (mimeTypes[0]) {
                        "image/*" -> {
                            ImageSelectActivity.start(activity, if (multipleSelect) Int.MAX_VALUE else 1, object : OnPhotoSelectListener {
                                override fun onSelected(data: MutableList<String>) {
                                    val uris: MutableList<Uri> = ArrayList(data.size)
                                    for (filePath in data) {
                                        uris.add(Uri.fromFile(File(filePath)))
                                    }
                                    callback.onReceiveValue(uris.toTypedArray())
                                }

                                override fun onCancel() {
                                    callback.onReceiveValue(null)
                                }
                            })
                            return
                        }
                        "video/*" -> {
                            VideoSelectActivity.start(activity, if (multipleSelect) Int.MAX_VALUE else 1, object : OnVideoSelectListener {

                                override fun onSelected(data: MutableList<VideoBean>) {
                                    val uris: MutableList<Uri> = ArrayList(data.size)
                                    for (bean in data) {
                                        uris.add(Uri.fromFile(File(bean.getVideoPath())))
                                    }
                                    callback.onReceiveValue(uris.toTypedArray())
                                }

                                override fun onCancel() {
                                    callback.onReceiveValue(null)
                                }
                            })
                            return
                        }
                    }
                }
            }

            // 是否是多选模式
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multipleSelect)
            activity.startActivityForResult(Intent.createChooser(intent, params.title), object : OnActivityCallback {

                override fun onActivityResult(resultCode: Int, data: Intent?) {
                    val uris: MutableList<Uri> = ArrayList()
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val uri = data.data
                        if (uri != null) {
                            // 如果用户只选择了一个文件
                            uris.add(uri)
                        } else {
                            // 如果用户选择了多个文件
                            val clipData = data.clipData
                            if (clipData != null) {
                                for (i in 0 until clipData.itemCount) {
                                    uris.add(clipData.getItemAt(i).uri)
                                }
                            }
                        }
                    }
                    // 不管用户最后有没有选择文件，最后必须要调用 onReceiveValue，如果没有调用就会导致网页再次点击上传无响应
                    callback.onReceiveValue(uris.toTypedArray())
                }
            })
        }
    }
}