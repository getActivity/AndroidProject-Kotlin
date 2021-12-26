package com.hjq.demo.http.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.util.ContentLengthInputStream
import okhttp3.*
import java.io.IOException
import java.io.InputStream

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/15
 *    desc   : OkHttp 加载器
 */
class OkHttpFetcher internal constructor(
    private val callFactory: Call.Factory,
    private val glideUrl: GlideUrl) :
    DataFetcher<InputStream>, Callback {

    private var inputStream: InputStream? = null
    private var responseBody: ResponseBody? = null
    private var dataCallback: DataFetcher.DataCallback<in InputStream?>? = null

    @Volatile
    private var call: Call? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        val requestBuilder: Request.Builder = Request.Builder().url(glideUrl.toStringUrl())
        for (headerEntry: MutableMap.MutableEntry<String, String> in glideUrl.headers.entries) {
            val key: String = headerEntry.key
            requestBuilder.addHeader(key, headerEntry.value)
        }
        val request: Request = requestBuilder.build()
        dataCallback = callback
        call = callFactory.newCall(request)
        call?.enqueue(this)
    }

    override fun onFailure(call: Call, e: IOException) {
        dataCallback?.onLoadFailed(e)
    }

    override fun onResponse(call: Call, response: Response) {
        responseBody = response.body()
        if (response.isSuccessful) {
            responseBody?.let {
                val contentLength: Long = it.contentLength()
                inputStream = ContentLengthInputStream.obtain(it.byteStream(), contentLength)
            }
            dataCallback?.onDataReady(inputStream)
        } else {
            dataCallback?.onLoadFailed(HttpException(response.message(), response.code()))
        }
    }

    override fun cleanup() {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        responseBody?.close()
        dataCallback = null
    }

    override fun cancel() {
        call?.cancel()
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }
}