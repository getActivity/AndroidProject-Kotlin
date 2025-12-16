package com.hjq.demo.widget.webview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.hjq.base.BaseActivity
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.createChooserIntent
import com.hjq.base.ktx.getActivity
import com.hjq.base.ktx.startActivityForResult
import com.hjq.demo.R
import com.hjq.demo.permission.PermissionDescription
import com.hjq.demo.permission.PermissionInterceptor
import com.hjq.demo.ui.dialog.common.InputDialog
import com.hjq.demo.ui.dialog.common.MessageDialog
import com.hjq.demo.ui.dialog.common.TipsDialog
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import timber.log.Timber

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/24
 *    desc   : 基于原生 WebChromeClient 封装
 */
open class BrowserChromeClient(private val browserView: BrowserView) : WebChromeClient() {

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        log(String.format("onProgressChanged: newProgress = %s", newProgress))
    }

    /**
     * 网页在控制台打印日志时回调
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        var priority = -1
        when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.TIP, ConsoleMessage.MessageLevel.LOG -> priority = Log.INFO
            ConsoleMessage.MessageLevel.WARNING -> priority = Log.WARN
            ConsoleMessage.MessageLevel.ERROR -> priority = Log.ERROR
            ConsoleMessage.MessageLevel.DEBUG -> priority = Log.DEBUG
            else -> {
                // default implementation ignored
            }
        }
        if (priority > 0) {
            // 打印一份网页的日志到 Logcat 上面
            Timber.log(
                priority, "WebView onConsoleMessage：lineNumber = %s, sourceId = %s, message = %s",
                consoleMessage.lineNumber().toString(), consoleMessage.sourceId(), consoleMessage.message()
            )
        }
        return super.onConsoleMessage(consoleMessage)
    }

    /**
     * 请求权限
     */
    override fun onPermissionRequest(request: PermissionRequest) {
        log(String.format("onPermissionRequest: requestOrigin = %s, requestResources = %s",
                          request.origin, request.resources.contentToString()))
        val permissions: MutableList<IPermission> = mutableListOf()
        val requestResources = request.resources
        if (requestResources == null) {
            // 如果网页请求的资源为空
            request.deny()
            return
        }
        for (resource in requestResources) {

            // 如果网页请求的是摄像头资源
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE == resource) {
                permissions.add(PermissionLists.getCameraPermission())
                continue
            }

            // 如果网页请求的是麦克风资源
            if (PermissionRequest.RESOURCE_AUDIO_CAPTURE == resource) {
                permissions.add(PermissionLists.getRecordAudioPermission())
                continue
            }

            // 如果网页请求的是别的资源
            request.deny()
            return
        }

        if (permissions.isEmpty()) {
            // 如果没有请求权限，则直接拒绝
            request.deny()
            return
        }

        val activity: Activity? = browserView.context.getActivity()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            if (XXPermissions.isGrantedPermissions(browserView.context, permissions)) {
                request.grant(requestResources)
            } else {
                request.deny()
            }
            return
        }

        XXPermissions.with(activity)
            .permissions(permissions)
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request { _, deniedList ->
                val allGranted = deniedList.isEmpty()
                if (!allGranted) {
                    request.deny()
                    return@request
                }
                request.grant(requestResources)
            }
    }

    /**
     * 网页弹出警告框
     */
    override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult?): Boolean {
        log(String.format("onJsAlert: url = %s, message = %s", url, message))
        val activity: Activity = browserView.context.getActivity() ?: return false
        TipsDialog.Builder(activity)
            .setIcon(TipsDialog.ICON_WARNING)
            .setMessage(message)
            .setCancelable(false)
            .addOnDismissListener {
                log("onJsAlert: call result.confirm()")
                result?.confirm()
            }
            .show()
        return true
    }

    /**
     * 网页弹出确定取消框
     */
    override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult?): Boolean {
        log(String.format("onJsConfirm: url = %s, message = %s", url, message))
        val activity: Activity = browserView.context.getActivity() ?: return false
        MessageDialog.Builder(activity)
            .setMessage(message)
            .setCancelable(false)
            .setListener(object : MessageDialog.OnListener {

                override fun onConfirm(dialog: BaseDialog) {
                    log("onJsConfirm: call result.confirm()")
                    result?.confirm()
                }

                override fun onCancel(dialog: BaseDialog) {
                    log("onJsConfirm: call result.cancel()")
                    result?.cancel()
                }
            })
            .show()
        return true
    }

    /**
     * 网页弹出输入框
     */
    override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult?): Boolean {
        log(String.format("onJsPrompt: url = %s, message = %s, defaultValue = %s", url, message, defaultValue))
        val activity: Activity = browserView.context.getActivity() ?: return false
        InputDialog.Builder(activity)
            .setContent(defaultValue)
            .setHint(message)
            .setCancelable(false)
            .setListener(object : InputDialog.OnListener {

                override fun onConfirm(dialog: BaseDialog, content: String) {
                    log(String.format("onJsPrompt: call result.confirm(%s)", content))
                    result?.confirm(content)
                }

                override fun onCancel(dialog: BaseDialog) {
                    log("onJsPrompt: call result.cancel()")
                    result?.cancel()
                }
            })
            .show()
        return true
    }

    /**
     * 网页请求定位功能
     * 测试地址：https://map.baidu.com/
     */
    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback?) {
        val activity: Activity = browserView.context.getActivity() ?: return
        MessageDialog.Builder(activity)
            .setMessage(R.string.common_web_location_permission_title)
            .setConfirm(R.string.common_web_location_permission_allow)
            .setCancel(R.string.common_web_location_permission_reject)
            .setCancelable(false)
            .setListener(object : MessageDialog.OnListener {

                override fun onConfirm(dialog: BaseDialog) {
                    XXPermissions.with(activity)
                        .permission(PermissionLists.getAccessFineLocationPermission())
                        .permission(PermissionLists.getAccessCoarseLocationPermission())
                        .interceptor(PermissionInterceptor())
                        .description(PermissionDescription())
                        .request { _, deniedList ->
                            val allGranted = deniedList.isEmpty()
                            if (!allGranted) {
                                return@request
                            }
                            log(String.format("onGeolocationPermissionsShowPrompt: callback.invoke(%s, true, true)", origin))
                            callback?.invoke(origin, true, true)
                        }
                }

                override fun onCancel(dialog: BaseDialog) {
                    log(String.format("onGeolocationPermissionsShowPrompt: callback.invoke(%s, false, true)", origin))
                    callback?.invoke(origin, false, true)
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
    override fun onShowFileChooser(webView: WebView, callback: ValueCallback<Array<Uri>>?, params: FileChooserParams): Boolean {
        log(String.format("onShowFileChooser: paramsTitle = %s, paramsMode = %s, paramsFilenameHint = %s, paramsAcceptTypes = %s",
                           params.title, params.mode, params.filenameHint, params.acceptTypes.contentToString()))
        val activity: Activity? = webView.context.getActivity()
        if (activity !is BaseActivity) {
            return false
        }

        openSystemFileChooser(activity, params, callback)
        return true
    }

    /**
     * 打开系统文件选择器
     */
    private fun openSystemFileChooser(activity: BaseActivity, params: FileChooserParams, callback: ValueCallback<Array<Uri>>?) {
        val intent: Intent = params.createIntent()
        // 是否是多选模式
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.mode == FileChooserParams.MODE_OPEN_MULTIPLE)
        activity.startActivityForResult(activity.createChooserIntent(intent, params.title), null, { resultCode, data ->
            val uris: MutableList<Uri> = mutableListOf()
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
            val result = uris.toTypedArray()
            log(String.format("onShowFileChooser: callback.onReceiveValue(%s)", result.contentToString()))
            callback?.onReceiveValue(result)
        })
    }

    protected fun log(message: String?) {
        Timber.i(message)
    }
}