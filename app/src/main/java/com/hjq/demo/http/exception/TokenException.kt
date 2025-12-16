package com.hjq.demo.http.exception

import com.hjq.http.exception.HttpException

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/12/19
 *    desc   : Token 失效异常
 */
class TokenException : HttpException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}