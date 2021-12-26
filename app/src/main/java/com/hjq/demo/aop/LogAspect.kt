package com.hjq.demo.aop

import android.os.Looper
import android.os.Trace
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.CodeSignature
import org.aspectj.lang.reflect.MethodSignature
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/06
 *    desc   : Debug 日志切面
 */
@Suppress("unused")
@Aspect
class LogAspect {

    /**
     * 构造方法切入点
     */
    @Pointcut("execution(@com.hjq.demo.aop.Log *.new(..))")
    fun constructor() {}

    /**
     * 方法切入点
     */
    @Pointcut("execution(@com.hjq.demo.aop.Log * *(..))")
    fun method() {}

    /**
     * 在连接点进行方法替换
     */
    @Around("(method() || constructor()) && @annotation(log)")
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint, log: Log): Any? {
        enterMethod(joinPoint, log)
        val startNanos: Long = System.nanoTime()
        val result: Any? = joinPoint.proceed()
        val stopNanos: Long = System.nanoTime()
        exitMethod(joinPoint, log, result, TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos))
        return result
    }

    /**
     * 方法执行前切入
     */
    private fun enterMethod(joinPoint: ProceedingJoinPoint, log: Log) {
        val codeSignature: CodeSignature = joinPoint.signature as CodeSignature

        // 方法所在类
        val className: String = codeSignature.declaringType.name
        // 方法名
        val methodName: String = codeSignature.name
        // 方法参数名集合
        val parameterNames: Array<String?> = codeSignature.parameterNames
        // 方法参数值集合
        val parameterValues: Array<Any?> = joinPoint.args

        //记录并打印方法的信息
        val builder: StringBuilder =
            getMethodLogInfo(className, methodName, parameterNames, parameterValues)
        log(log.value, builder.toString())
        val section: String = builder.substring(2)
        Trace.beginSection(section)
    }

    /**
     * 获取方法的日志信息
     *
     * @param className         类名
     * @param methodName        方法名
     * @param parameterNames    方法参数名集合
     * @param parameterValues   方法参数值集合
     */
    private fun getMethodLogInfo(className: String, methodName: String, parameterNames: Array<String?>, parameterValues: Array<Any?>): StringBuilder {
        val builder: StringBuilder = StringBuilder("\u21E2 ")
        builder.append(className)
            .append(".")
            .append(methodName)
            .append('(')
        for (i in parameterValues.indices) {
            if (i > 0) {
                builder.append(", ")
            }
            builder.append(parameterNames[i]).append('=')
            builder.append(parameterValues[i].toString())
        }
        builder.append(')')
        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().name).append("\"]")
        }
        return builder
    }

    /**
     * 方法执行完毕，切出
     *
     * @param result            方法执行后的结果
     * @param lengthMillis      执行方法所需要的时间
     */
    private fun exitMethod(joinPoint: ProceedingJoinPoint, log: Log, result: Any?, lengthMillis: Long) {
        Trace.endSection()
        val signature: Signature = joinPoint.signature
        val className: String? = signature.declaringType.name
        val methodName: String? = signature.name
        val builder: StringBuilder = StringBuilder("\u21E0 ")
            .append(className)
            .append(".")
            .append(methodName)
            .append(" [")
            .append(lengthMillis)
            .append("ms]")

        //  判断方法是否有返回值
        if (signature is MethodSignature && signature.returnType != Void.TYPE) {
            builder.append(" = ")
            builder.append(result.toString())
        }
        log(log.value, builder.toString())
    }

    private fun log(tag: String?, msg: String?) {
        Timber.tag(tag)
        Timber.d(msg)
    }
}