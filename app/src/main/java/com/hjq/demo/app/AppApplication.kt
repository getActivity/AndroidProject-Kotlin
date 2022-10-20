package com.hjq.demo.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.hjq.bar.TitleBar
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.http.model.RequestHandler
import com.hjq.demo.http.model.RequestServer
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.other.*
import com.hjq.gson.factory.GsonFactory
import com.hjq.http.EasyConfig
import com.hjq.http.config.IRequestApi
import com.hjq.http.model.HttpHeaders
import com.hjq.http.model.HttpParams
import com.hjq.toast.ToastUtils
import com.hjq.umeng.UmengClient
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 应用入口
 */
class AppApplication : Application() {

    @Log("启动耗时")
    override fun onCreate() {
        super.onCreate()
        initSdk(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // 清理所有图片内存缓存
        GlideApp.get(this).onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // 根据手机内存剩余情况清理图片内存缓存
        GlideApp.get(this).onTrimMemory(level)
    }

    companion object {

        /**
         * 初始化一些第三方框架
         */
        fun initSdk(application: Application) {
            // 设置标题栏初始化器
            TitleBar.setDefaultStyle(TitleBarStyle())

            // 设置全局的 Header 构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator{ context: Context, layout: RefreshLayout ->
                MaterialHeader(context).setColorSchemeColors(ContextCompat.getColor(context, R.color.common_accent_color))
            }
            // 设置全局的 Footer 构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator{ context: Context, layout: RefreshLayout ->
                SmartBallPulseFooter(context)
            }
            // 设置全局初始化器
            SmartRefreshLayout.setDefaultRefreshInitializer { context: Context, layout: RefreshLayout ->
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
            }

            // 初始化吐司
            ToastUtils.init(application, ToastStyle())
            // 设置调试模式
            ToastUtils.setDebugMode(AppConfig.isDebug())
            // 设置 Toast 拦截器
            ToastUtils.setInterceptor(ToastLogInterceptor())

            // 本地异常捕捉
            CrashHandler.register(application)

            // 友盟统计、登录、分享 SDK
            UmengClient.init(application, AppConfig.isLogEnable())

            // Bugly 异常捕捉
            CrashReport.initCrashReport(application, AppConfig.getBuglyId(), AppConfig.isDebug())

            // Activity 栈管理初始化
            ActivityManager.getInstance().init(application)

            // MMKV 初始化
            MMKV.initialize(application)

            // 网络请求框架初始化
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .build()

            EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(AppConfig.isLogEnable())
                // 设置服务器配置
                .setServer(RequestServer())
                // 设置请求处理策略
                .setHandler(RequestHandler(application))
                // 设置请求重试次数
                .setRetryCount(1)
                .addHeader("token","666")
                .addHeader("deviceOaid", UmengClient.getDeviceOaid())
                .addHeader("versionName",  AppConfig.getVersionName())
                .addHeader("versionCode", AppConfig.getVersionCode().toString())
                .into()

            // 设置 Json 解析容错监听
            GsonFactory.setJsonCallback { typeToken: TypeToken<*>, fieldName: String?, jsonToken: JsonToken ->
                // 上报到 Bugly 错误列表
                CrashReport.postCatchedException(IllegalArgumentException("类型解析异常：$typeToken#$fieldName，后台返回的类型为：$jsonToken"))
            }

            // 初始化日志打印
            if (AppConfig.isLogEnable()) {
                Timber.plant(DebugLoggerTree())
            }

            // 注册网络状态变化监听
            val connectivityManager: ConnectivityManager? = ContextCompat.getSystemService(application, ConnectivityManager::class.java)
            if (connectivityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onLost(network: Network) {
                        val topActivity: Activity? = ActivityManager.getInstance().getTopActivity()
                        if (topActivity !is LifecycleOwner) {
                            return
                        }
                        val lifecycleOwner: LifecycleOwner = topActivity
                        if (lifecycleOwner.lifecycle.currentState != Lifecycle.State.RESUMED) {
                            return
                        }
                        ToastUtils.show(R.string.common_network_error)
                    }
                })
            }
        }
    }
}