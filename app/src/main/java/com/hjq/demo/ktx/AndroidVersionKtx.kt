package com.hjq.demo.ktx

import android.os.Build

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2025/12/12
 *    desc   : Android 版本判断调用扩展
 */
/** [Build.VERSION_CODES.BAKLAVA]  */
const val ANDROID_16: Int = 36

/** [Build.VERSION_CODES.VANILLA_ICE_CREAM]  */
const val ANDROID_15: Int = 35

/** [Build.VERSION_CODES.UPSIDE_DOWN_CAKE]  */
const val ANDROID_14: Int = 34

/** [Build.VERSION_CODES.TIRAMISU]  */
const val ANDROID_13: Int = 33

/** [Build.VERSION_CODES.S_V2]  */
const val ANDROID_12_1: Int = 32

/** [Build.VERSION_CODES.S]  */
const val ANDROID_12: Int = 31

/** [Build.VERSION_CODES.R]  */
const val ANDROID_11: Int = 30

/** [Build.VERSION_CODES.Q]  */
const val ANDROID_10: Int = 29

/** [Build.VERSION_CODES.P]  */
const val ANDROID_9: Int = 28

/** [Build.VERSION_CODES.O_MR1]  */
const val ANDROID_8_1: Int = 27

/** [Build.VERSION_CODES.O]  */
const val ANDROID_8: Int = 26

/** [Build.VERSION_CODES.N_MR1]  */
const val ANDROID_7_1: Int = 25

/** [Build.VERSION_CODES.N]  */
const val ANDROID_7: Int = 24

/**
 * 是否是 Android 16 及以上版本
 */
fun isAndroid16(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_16
}

/**
 * 是否是 Android 15 及以上版本
 */
fun isAndroid15(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_15
}

/**
 * 是否是 Android 14 及以上版本
 */
fun isAndroid14(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_14
}

/**
 * 是否是 Android 13 及以上版本
 */
fun isAndroid13(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_13
}

/**
 * 是否是 Android 12.1（又称 Android 12L）及以上版本
 */
fun isAndroid12_1(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_12_1
}

/**
 * 是否是 Android 12 及以上版本
 */
fun isAndroid12(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_12
}

/**
 * 是否是 Android 11 及以上版本
 */
fun isAndroid11(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_11
}

/**
 * 是否是 Android 10 及以上版本
 */
fun isAndroid10(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_10
}

/**
 * 是否是 Android 9.0 及以上版本
 */
fun isAndroid9(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_9
}

/**
 * 是否是 Android 8.1 及以上版本
 */
fun isAndroid8_1(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_8_1
}

/**
 * 是否是 Android 8.0 及以上版本
 */
fun isAndroid8(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_8
}

/**
 * 是否是 Android 7.1 及以上版本
 */
fun isAndroid7_1(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_7_1
}

/**
 * 是否是 Android 7.0 及以上版本
 */
fun isAndroid7(): Boolean {
    return Build.VERSION.SDK_INT >= ANDROID_7
}