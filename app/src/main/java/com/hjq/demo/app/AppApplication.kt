package com.hjq.demo.app

import android.app.Application
import com.hjq.core.manager.ActivityManager
import com.hjq.demo.aop.Log
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.manager.InitManager

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

        // 如果当前的进程不是主进程的话，则不进行第三方框架的初始化
        if (!ActivityManager.isMainProcess(this)) {
            return
        }

        InitManager.preInitSdk(this)
        if (InitManager.isAgreePrivacy(this)) {
            InitManager.initSdk(this);
        }
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
}