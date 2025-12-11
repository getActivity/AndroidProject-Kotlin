package com.hjq.demo.aop

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import timber.log.Timber

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/12/06
 * desc   : 防重复点击切面
 */
class SingleClickCut : BasePointCut<SingleClick> {

    companion object {

        private var lastTime: Long = 0

        private var lastTag: String? = null
    }

    @Throws(Throwable::class)
    override fun invoke(joinPoint: ProceedJoinPoint, anno: SingleClick): Any? {
        val className = if (joinPoint.target != null) joinPoint.target!!.javaClass.getName() else ""
        val methodName = joinPoint.targetMethod.name
        val parameterValues = joinPoint.args

        val builder = StringBuilder(className).append(".").append(methodName).append("(")
        for (i in 0..<(parameterValues?.size ?: 0)) {
            if (i == 0) {
                builder.append(parameterValues!![i])
            } else {
                builder.append(", ").append(parameterValues!![i])
            }
        }
        builder.append(")")
        val tag = builder.toString()

        val now = System.currentTimeMillis()
        if (now - lastTime < anno.value && tag == lastTag) {
            Timber.tag("SingleClick")
            Timber.i("%d 毫秒内发生快速点击：%s", anno.value, tag)
            return null
        }
        lastTime = now
        lastTag = tag
        return joinPoint.proceed()
    }
}