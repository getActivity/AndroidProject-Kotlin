package com.hjq.demo.aop

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/06
 *    desc   : 防重复点击注解
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@AndroidAopPointCut(SingleClickCut::class)
annotation class SingleClick(
    /**
     * 快速点击的间隔
     */
    val value: Long = 1000
)