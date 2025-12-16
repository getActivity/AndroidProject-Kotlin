package com.hjq.demo.http.model

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 统一接口数据结构
 */
open class HttpData<T> {

    /** 响应头 */
    private var responseHeaders: MutableMap<String, String>? = null

    /** 返回码 */
    private val code: Int = 0

    /** 提示语 */
    private val msg: String? = null

    /** 数据 */
    private val data: T? = null

    fun setResponseHeaders(headers: MutableMap<String, String>?) {
        this.responseHeaders = headers
    }

    fun getResponseHeaders(): MutableMap<String, String>? {
        return responseHeaders
    }

    fun getCode(): Int {
        return code
    }

    fun getMessage(): String {
        return msg ?: ""
    }

    fun getData(): T? {
        return data
    }

    /**
     * 是否请求成功
     */
    fun isRequestSuccess(): Boolean {
        return code == 200
    }

    /**
     * 是否 Token 失效
     */
    fun isTokenInvalidation(): Boolean {
        return code == 1001
    }
}