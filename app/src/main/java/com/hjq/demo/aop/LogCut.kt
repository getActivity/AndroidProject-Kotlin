package com.hjq.demo.aop

import android.annotation.SuppressLint
import android.os.Looper
import android.os.Trace
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/06
 *    desc   : 防重复点击切面
 */
class LogCut : BasePointCut<Log> {

    @Throws(Throwable::class)
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Log): Any? {
        enterMethod(joinPoint, anno)
        val startNanos = System.nanoTime()
        val result = joinPoint.proceed()
        val stopNanos = System.nanoTime()
        exitMethod(joinPoint, anno, result, TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos))
        return result
    }

    @SuppressLint("UnclosedTrace")
    private fun enterMethod(joinPoint: ProceedJoinPoint, log: Log) {
        val className = joinPoint.target?.javaClass?.name ?: ""
        val methodName = joinPoint.targetMethod.name
        val parameterNames: Array<String?>? = null
        val parameterValues = joinPoint.args

        val builder = getMethodLogInfo(className, methodName, parameterNames, parameterValues)
        log(log.value, builder.toString())
        val section = builder.substring(2)
        Trace.beginSection(section)
    }

    private fun getMethodLogInfo(
        className: String?,
        methodName: String?,
        parameterNames: Array<String?>?,
        parameterValues: Array<Any?>?
    ): StringBuilder {
        val builder = StringBuilder("\u21E2 ")
        builder.append(className).append(".").append(methodName).append('(')
        if (parameterValues != null && parameterNames != null) {
            for (i in parameterValues.indices) {
                if (i > 0) {
                    builder.append(", ")
                }
                builder.append(parameterNames[i]).append('=')
                builder.append(parameterValues[i])
            }
        }
        builder.append(')')
        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().name).append("\"]")
        }
        return builder
    }

    private fun exitMethod(joinPoint: ProceedJoinPoint, log: Log, result: Any?, lengthMillis: Long) {
        Trace.endSection()
        val className = joinPoint.target?.javaClass?.name ?: ""
        val methodName = joinPoint.targetMethod.name
        val builder = StringBuilder("\u21E0 ")
            .append(className)
            .append('.')
            .append(methodName)
            .append(" [")
            .append(lengthMillis)
            .append("ms]")
        if (result != null) {
            builder.append(" = ").append(result)
        }
        log(log.value, builder.toString())
    }

    private fun log(tag: String, msg: String?) {
        Timber.tag(tag)
        Timber.d(msg)
    }
}