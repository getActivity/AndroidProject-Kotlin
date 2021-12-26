package com.hjq.demo.http.api

import com.hjq.http.config.IRequestApi

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 修改手机
 */
class PhoneApi : IRequestApi {

    override fun getApi(): String {
        return "user/phone"
    }

    /** 旧手机号验证码（没有绑定情况下可不传） */
    private var preCode: String? = null

    /** 新手机号 */
    private var phone: String? = null

    /** 新手机号验证码 */
    private var code: String? = null

    fun setPreCode(preCode: String?): PhoneApi = apply {
        this.preCode = preCode
    }

    fun setPhone(phone: String?): PhoneApi = apply {
        this.phone = phone
    }

    fun setCode(code: String?): PhoneApi = apply {
        this.code = code
    }
}