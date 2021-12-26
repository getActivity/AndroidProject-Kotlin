package com.hjq.demo.manager

import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/01/11
 *    desc   : 线程池管理类
 */
class ThreadPoolManager : ThreadPoolExecutor(
    0, 200,
    30L, TimeUnit.MILLISECONDS,
    SynchronousQueue()) {

    companion object {

        @Volatile
        private var instance: ThreadPoolManager? = null

        fun getInstance(): ThreadPoolManager {
            if (instance == null) {
                synchronized(ThreadPoolManager::class.java) {
                    if (instance == null) {
                        instance = ThreadPoolManager()
                    }
                }
            }
            return instance!!
        }
    }
}