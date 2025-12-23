package com.hjq.demo.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.hjq.bar.TitleBar
import com.hjq.core.ktx.isAndroid7
import com.hjq.core.manager.ActivityManager
import com.hjq.demo.R
import com.hjq.demo.http.model.HttpCacheStrategy
import com.hjq.demo.http.model.RequestHandler
import com.hjq.demo.http.model.RequestServer
import com.hjq.demo.ktx.toast
import com.hjq.demo.other.AppConfig
import com.hjq.demo.other.CrashHandler
import com.hjq.demo.other.DebugLoggerTree
import com.hjq.demo.other.MaterialHeader
import com.hjq.demo.other.SmartBallPulseFooter
import com.hjq.demo.other.TitleBarStyle
import com.hjq.demo.other.ToastInterceptor
import com.hjq.demo.other.ToastStyle
import com.hjq.gson.factory.GsonFactory
import com.hjq.gson.factory.ParseExceptionCallback
import com.hjq.http.EasyConfig
import com.hjq.http.config.IRequestInterceptor
import com.hjq.http.model.HttpHeaders
import com.hjq.http.model.HttpParams
import com.hjq.http.request.HttpRequest
import com.hjq.toast.Toaster
import com.hjq.umeng.sdk.UmengClient
import com.hjq.umeng.sdk.UmengClient.getDeviceOaid
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.tencent.bugly.library.Bugly
import com.tencent.bugly.library.BuglyBuilder
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2023/06/24
 *    desc   : 初始化管理器
 */
object InitManager {

    /** 隐私政策配置文件  */
    private const val AGREE_PRIVACY_NAME = "agree_privacy_config"

    /** 隐私政策同意结果  */
    private const val KEY_AGREE_PRIVACY_RESULT = "key_agree_privacy_result"

    /**
     * 是否同意了隐私协议
     */
    fun isAgreePrivacy(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(AGREE_PRIVACY_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_AGREE_PRIVACY_RESULT, false)
    }

    /**
     * 设置隐私协议结果
     */
    fun setAgreePrivacy(context: Context, result: Boolean) {
        val sharedPreferences = context.getSharedPreferences(AGREE_PRIVACY_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit { putBoolean(KEY_AGREE_PRIVACY_RESULT, result) }
    }

    /**
     * 预初始化第三方 SDK
     */
    fun preInitSdk(application: Application) {
        // 初始化日志打印
        if (AppConfig.isLogEnable()) {
            Timber.plant(DebugLoggerTree())
        }

        // 设置标题栏全局样式
        TitleBar.setGlobalStyle(TitleBarStyle())

        // 设置全局的 Header 构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator{ context: Context, _: RefreshLayout ->
            MaterialHeader(context).setColorSchemeColors(ContextCompat.getColor(context, R.color.common_accent_color))
        }
        // 设置全局的 Footer 构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator{ context: Context, _: RefreshLayout ->
            SmartBallPulseFooter(context)
        }
        // 设置全局初始化器
        SmartRefreshLayout.setDefaultRefreshInitializer { _: Context, layout: RefreshLayout ->
            // 刷新头部是否跟随内容偏移
            layout.setEnableHeaderTranslationContent(true)
                // 刷新尾部是否跟随内容偏移
                .setEnableFooterTranslationContent(true)
                // 加载更多是否跟随内容偏移
                .setEnableFooterFollowWhenNoMoreData(true)
                // 内容不满一页时是否可以上拉加载更多
                .setEnableLoadMoreWhenContentNotFull(false)
                // 仿苹果越界效果开关
                .setEnableOverScrollDrag(false)

            // 关闭框架预埋的彩蛋
            // https://github.com/scwang90/SmartRefreshLayout/issues/1105
            layout.layout.tag = "close egg"
        }

        // 初始化吐司
        Toaster.init(application, ToastStyle())
        // 设置调试模式
        Toaster.setDebugMode(AppConfig.isDebug())
        // 设置 Toast 拦截器
        Toaster.setInterceptor(ToastInterceptor())

        // 本地异常捕捉
        CrashHandler.register(application)

        // 友盟统计、登录、分享 SDK
        UmengClient.init(application, AppConfig.isLogEnable())

        // Bugly 异常捕捉
        val builder = BuglyBuilder(AppConfig.getBuglyId(), AppConfig.getBuglyKey())
        builder.debugMode = AppConfig.isDebug()
        Bugly.init(application, builder)

        // Activity 栈管理初始化
        ActivityManager.init(application)

        // MMKV 初始化
        MMKV.initialize(application)

        // 网络请求框架初始化
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor(application))
            .build()

        EasyConfig.with(okHttpClient)
            // 是否打印日志
            .setLogEnabled(AppConfig.isLogEnable())
            // 设置服务器配置
            .setServer(RequestServer())
            // 设置请求处理策略
            .setHandler(RequestHandler(application))
            // 设置请求缓存实现策略（非必须）
            .setCacheStrategy(HttpCacheStrategy())
            // 设置请求重试次数
            .setRetryCount(1)
            .setInterceptor(object : IRequestInterceptor {
                override fun interceptArguments(
                    httpRequest: HttpRequest<*>,
                    params: HttpParams,
                    headers: HttpHeaders) {
                    // 添加全局请求头
                    headers.put("token", "66666666666")
                    headers.put("deviceOaid", getDeviceOaid())
                    headers.put("versionName", AppConfig.getVersionName())
                    headers.put("versionCode", AppConfig.getVersionCode().toString())
                    // 添加全局请求参数
                    // params.put("6666666", "6666666");
                }
            })
            .into()

        // 设置 Json 解析容错监听
        GsonFactory.setParseExceptionCallback(object : ParseExceptionCallback {

            override fun onParseObjectException(typeToken: TypeToken<*>, fieldName: String, jsonToken: JsonToken) {
                handlerGsonParseException("解析对象析异常：$typeToken#$fieldName，后台返回的类型为：$jsonToken")
            }

            override fun onParseListItemException(typeToken: TypeToken<*>, fieldName: String, listItemJsonToken: JsonToken) {
                handlerGsonParseException("解析 List 异常：$typeToken#$fieldName，后台返回的条目类型为：$listItemJsonToken")
            }

            override fun onParseMapItemException(typeToken: TypeToken<*>, fieldName: String, mapItemKey: String, mapItemJsonToken: JsonToken) {
                handlerGsonParseException("解析 Map 异常：$typeToken#$fieldName，mapItemKey = $mapItemKey，后台返回的条目类型为：$mapItemJsonToken")
            }

            private fun handlerGsonParseException(message: String) {
                val e = IllegalArgumentException(message)
                if (AppConfig.isDebug()) {
                    throw e
                } else {
                    // 上报到 Bugly 错误列表中
                    Bugly.handleCatchException(Thread.currentThread(), e, e.message, null, true)
                }
            }
        })

        // 注册网络状态变化监听
        val connectivityManager: ConnectivityManager? = ContextCompat.getSystemService(application, ConnectivityManager::class.java)
        if (connectivityManager != null && isAndroid7()) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    val topActivity: Activity? = ActivityManager.getTopActivity()
                    if (topActivity !is LifecycleOwner) {
                        return
                    }
                    val lifecycleOwner: LifecycleOwner = topActivity
                    if (lifecycleOwner.lifecycle.currentState != Lifecycle.State.RESUMED) {
                        return
                    }
                    toast(R.string.common_network_error)
                }
            })
        }

        // 预初始化友盟 SDK
        UmengClient.preInit(application, AppConfig.isLogEnable())
    }

    /**
     * 初始化第三方 SDK
     */
    fun initSdk(application: Application) {
        // 友盟统计、登录、分享 SDK
        UmengClient.init(application, AppConfig.isLogEnable())
    }
}