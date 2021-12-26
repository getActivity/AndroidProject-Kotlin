package com.hjq.demo.http.api

import com.hjq.http.config.IRequestApi

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 用户注册
 */
class RegisterApi : IRequestApi {

    override fun getApi(): String {
        return "user/register"
    }

    /** 手机号 */
    private var phone: String? = null

    /** 验证码 */
    private var code: String? = null

    /** 密码 */
    private var password: String? = null

    fun setPhone(phone: String?): RegisterApi = apply {
        this.phone = phone
    }

    fun setCode(code: String?): RegisterApi = apply {
        this.code = code
    }

    fun setPassword(password: String?): RegisterApi = apply {
        this.password = password
    }

    class Bean {

    }
}