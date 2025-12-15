package com.hjq.demo.http.model

import com.hjq.demo.other.AppConfig
import com.hjq.http.config.IHttpPostBodyStrategy
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.RequestBodyType

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/10/02
 *    desc   : 服务器配置
 */
class RequestServer : IRequestServer {

    override fun getHost(): String {
        return AppConfig.getHostUrl() + "api/"
    }

    override fun getBodyType(): IHttpPostBodyStrategy {
        // 以表单的形式提交参数
        return RequestBodyType.FORM
    }
}