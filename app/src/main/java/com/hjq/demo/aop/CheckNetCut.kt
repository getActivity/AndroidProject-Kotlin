package com.hjq.demo.aop

import android.app.Application
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.hjq.demo.R
import com.hjq.demo.manager.ActivityManager
import com.hjq.toast.Toaster

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/01/11
 *    desc   : 网络检测切面
 */
class CheckNetCut : BasePointCut<CheckNet> {

    @Suppress("deprecation")
    @Throws(Throwable::class)
    override fun invoke(joinPoint: ProceedJoinPoint, anno: CheckNet): Any? {
        val application: Application = ActivityManager.getApplication()
        val manager = ContextCompat.getSystemService(application, ConnectivityManager::class.java)
        if (manager != null) {
            val info = manager.activeNetworkInfo
            if (info == null || !info.isConnected) {
                Toaster.show(R.string.common_network_hint)
                return null
            }
        }
        return joinPoint.proceed()
    }
}