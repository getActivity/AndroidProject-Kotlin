package com.hjq.demo.aop

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/01/11
 *    desc   : 网络检测注解
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@AndroidAopPointCut(CheckNetCut::class)
annotation class CheckNet