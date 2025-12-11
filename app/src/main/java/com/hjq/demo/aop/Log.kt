package com.hjq.demo.aop

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/06
 *    desc   : Debug 日志注解
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@AndroidAopPointCut(LogCut::class)
annotation class Log(val value: String = "AOPLog")