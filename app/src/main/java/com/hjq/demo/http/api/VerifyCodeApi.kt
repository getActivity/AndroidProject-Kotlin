package com.hjq.demo.http.api

import com.hjq.http.config.IRequestApi

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 验证码校验
 */
class VerifyCodeApi : IRequestApi {

    override fun getApi(): String {
        return "code/checkout"
    }

    /** 手机号 */
    private var phone: String? = null

    /** 验证码 */
    private var code: String? = null

    fun setPhone(phone: String?): VerifyCodeApi = apply {
        this.phone = phone
    }

    fun setCode(code: String?): VerifyCodeApi = apply {
        this.code = code
    }
}