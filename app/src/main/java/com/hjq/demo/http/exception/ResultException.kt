package com.hjq.demo.http.exception

import com.hjq.demo.http.model.HttpData
import com.hjq.http.exception.HttpException

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2021/12/19
 *    desc   : 返回结果异常
 */
class ResultException : HttpException {

    val httpData: HttpData<*>

    constructor(message: String, data: HttpData<*>) : super(message) {
        httpData = data
    }

    constructor(message: String, cause: Throwable, data: HttpData<*>) : super(message, cause) {
        httpData = data
    }
}