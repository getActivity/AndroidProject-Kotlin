package com.hjq.demo.http.api

import com.hjq.http.config.IRequestApi

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 获取用户信息
 */
class UserInfoApi : IRequestApi {

    override fun getApi(): String {
        return "user/info"
    }

    class Bean {

    }
}