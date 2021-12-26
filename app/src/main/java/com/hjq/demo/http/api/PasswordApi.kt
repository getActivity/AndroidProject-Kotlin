package com.hjq.demo.http.api

import com.hjq.http.config.IRequestApi

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 修改密码
 */
class PasswordApi : IRequestApi {

    override fun getApi(): String {
        return "user/password"
    }

    /** 手机号（已登录可不传） */
    private var phone: String? = null

    /** 验证码 */
    private var code: String? = null

    /** 密码 */
    private var password: String? = null

    fun setPhone(phone: String?): PasswordApi = apply {
        this.phone = phone
    }

    fun setCode(code: String?): PasswordApi = apply {
        this.code = code
    }

    fun setPassword(password: String?): PasswordApi = apply {
        this.password = password
    }
}