package com.hjq.demo.aop

import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.core.content.ContextCompat
import com.hjq.demo.R
import com.hjq.demo.ktx.toast
import com.hjq.demo.manager.ActivityManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/01/11
 *    desc   : 网络检测切面
 */
@Suppress("unused")
@Aspect
class CheckNetAspect {

    /**
     * 方法切入点
     */
    @Pointcut("execution(@com.hjq.demo.aop.CheckNet * *(..))")
    fun method() {}

    /**
     * 在连接点进行方法替换
     */
    @Around("method() && @annotation(checkNet)")
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint, checkNet: CheckNet) {
        val manager: ConnectivityManager? = ContextCompat.getSystemService(
            ActivityManager.getApplication(), ConnectivityManager::class.java)
        if (manager != null) {
            val info: NetworkInfo? = manager.activeNetworkInfo
            // 判断网络是否连接
            if (info == null || !info.isConnected) {
                toast(R.string.common_network_hint)
                return
            }
        }
        //执行原方法
        joinPoint.proceed()
    }
}