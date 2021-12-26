package com.hjq.demo.http.api

import com.hjq.http.config.IRequestApi
import java.io.File

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 上传图片
 */
class UpdateImageApi : IRequestApi {

    override fun getApi(): String {
        return "update/image"
    }

    /** 图片文件 */
    private var image: File? = null

    fun setImage(image: File?): UpdateImageApi = apply {
        this.image = image
    }
}