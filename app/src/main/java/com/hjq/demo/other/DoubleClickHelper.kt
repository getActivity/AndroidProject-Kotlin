package com.hjq.demo.other

import android.os.SystemClock

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 双击判断工具类
 */
object DoubleClickHelper {

    /** 数组的长度为2代表只记录双击操作 */
    private val TIME_ARRAY: LongArray = LongArray(2)

    /**
     * 是否在短时间内进行了双击操作
     */
    fun isOnDoubleClick(): Boolean {
        // 默认间隔时长
        return isOnDoubleClick(1500)
    }

    /**
     * 是否在短时间内进行了双击操作
     */
    fun isOnDoubleClick(time: Int): Boolean {
        System.arraycopy(TIME_ARRAY, 1, TIME_ARRAY, 0, TIME_ARRAY.size - 1)
        TIME_ARRAY[TIME_ARRAY.size - 1] = SystemClock.uptimeMillis()
        return TIME_ARRAY[0] >= (SystemClock.uptimeMillis() - time)
    }
}