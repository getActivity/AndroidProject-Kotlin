package com.hjq.demo.http.model

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.gson.JsonSyntaxException
import com.hjq.base.ktx.createIntent
import com.hjq.demo.R
import com.hjq.demo.http.exception.ResultException
import com.hjq.demo.http.exception.TokenException
import com.hjq.demo.http.model.HttpCacheManager.generateCacheKey
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.ui.activity.account.LoginActivity
import com.hjq.gson.factory.GsonFactory
import com.hjq.http.EasyLog
import com.hjq.http.config.IRequestHandler
import com.hjq.http.exception.*
import com.hjq.http.request.HttpRequest
import okhttp3.Headers
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/07
 *    desc   : 请求处理类
 */
class RequestHandler constructor(private val application: Application) : IRequestHandler {

    @Throws(Throwable::class)
    override fun requestSuccess(httpRequest: HttpRequest<*>, response: Response, type: Type): Any {

        if ((Response::class.java == type)) {
            return response
        }

        if (!response.isSuccessful) {
            throw ResponseException(String.format(application.getString(R.string.http_response_error),
                    response.code(), response.message()), response)
        }

        if ((Headers::class.java == type)) {
            return response.headers()
        }

        val body = response.body()
            ?: throw NullBodyException(application.getString(R.string.http_response_null_body))

        if ((InputStream::class.java == type)) {
            return body.byteStream()
        }

        if (Bitmap::class.java == type) {
            return BitmapFactory.decodeStream(body.byteStream())
        }

        val text: String
        try {
            text = body.string()
        } catch (e: IOException) {
            // 返回结果读取异常
            throw DataException(application.getString(R.string.http_data_explain_error), e)
        }

        // 打印这个 Json 或者文本
        EasyLog.printJson(httpRequest, text)
        if ((String::class.java == type)) {
            return text
        }

        val result: Any
        try {
            result = GsonFactory.getSingletonGson().fromJson(text, type)
        } catch (e: JsonSyntaxException) {
            // 返回结果读取异常
            throw DataException(application.getString(R.string.http_data_explain_error), e)
        }

        if (result is HttpData<*>) {
            val model: HttpData<*> = result
            val headers = response.headers()
            val headersSize = headers.size()
            val headersMap: MutableMap<String, String> = HashMap(headersSize)
            for (i in 0 until headersSize) {
                headersMap[headers.name(i)] = headers.value(i)
            }
            // Github issue 地址：https://github.com/getActivity/EasyHttp/issues/233
            model.setResponseHeaders(headersMap)

            if (model.isRequestSuccess()) {
                // 代表执行成功
                return result
            }

            if (model.isTokenInvalidation()) {
                // 代表登录失效，需要重新登录
                throw TokenException(application.getString(R.string.http_token_error))
            }

            throw ResultException(model.getMessage(), model)
        }
        return result
    }

    override fun requestFail(httpRequest: HttpRequest<*>, throwable: Throwable): Throwable {
        if (throwable is HttpException) {
            if (throwable is TokenException) {
                // 登录信息失效，跳转到登录页
                val application: Application = ActivityManager.getApplication()
                val intent = application.createIntent(LoginActivity::class.java)
                application.startActivity(intent)
                // 销毁除了登录页之外的 Activity
                ActivityManager.finishAllActivities(LoginActivity::class.java)
            }
            return throwable
        }
        if (throwable is SocketTimeoutException) {
            return TimeoutException(application.getString(R.string.http_server_out_time), throwable)
        }
        if (throwable is UnknownHostException) {
            val info: NetworkInfo? = (application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            // 判断网络是否连接
            if (info == null || !info.isConnected) {
                // 没有连接就是网络异常
                return NetworkException(application.getString(R.string.http_network_error), throwable)
            }

            // 有连接就是服务器的问题
            return ServerException(application.getString(R.string.http_server_error), throwable)
        }
        if (throwable is IOException) {
            // 出现该异常的两种情况
            // 1. 调用 EasyHttp.cancel
            // 2. 网络请求被中断
            return CancelException(application.getString(R.string.http_request_cancel), throwable)
        }
        return HttpException(throwable.message, throwable)
    }

    override fun downloadFail(httpRequest: HttpRequest<*>, throwable: Throwable): Throwable {
        when (throwable) {
            is ResponseException -> {
                val response = throwable.response
                throwable.setMessage(
                    String.format(
                        application.getString(R.string.http_response_error),
                        response.code(), response.message()
                    )
                )
                return throwable
            }
            is NullBodyException -> {
                throwable.setMessage(application.getString(R.string.http_response_null_body))
                return throwable
            }
            is FileMd5Exception -> {
                throwable.setMessage(application.getString(R.string.http_response_md5_error))
                return throwable
            }
            else -> return requestFail(httpRequest, throwable)
        }
    }

    override fun readCache(httpRequest: HttpRequest<*>, type: Type, cacheTime: Long): Any? {
        val cacheKey = generateCacheKey(httpRequest)
        val cacheValue = HttpCacheManager.readHttpCache(cacheKey)
        if (cacheValue == null || "" == cacheValue || "{}" == cacheValue) {
            return null
        }
        EasyLog.printLog(httpRequest, "----- read cache key -----")
        EasyLog.printJson(httpRequest, cacheKey)
        EasyLog.printLog(httpRequest, "----- read cache value -----")
        EasyLog.printJson(httpRequest, cacheValue)
        EasyLog.printLog(httpRequest, "cacheTime = $cacheTime")
        val cacheInvalidate = HttpCacheManager.isCacheInvalidate(cacheKey, cacheTime)
        EasyLog.printLog(httpRequest, "cacheInvalidate = $cacheInvalidate")
        return if (cacheInvalidate) {
            // 表示缓存已经过期了，直接返回 null 给外层，表示缓存不可用
            null
        } else GsonFactory.getSingletonGson().fromJson<Any>(cacheValue, type)
    }

    override fun writeCache(httpRequest: HttpRequest<*>, response: Response, result: Any): Boolean {
        val cacheKey = generateCacheKey(httpRequest)
        val cacheValue = GsonFactory.getSingletonGson().toJson(result)
        if (cacheValue == null || "" == cacheValue || "{}" == cacheValue) {
            return false
        }
        EasyLog.printLog(httpRequest, "----- write cache key -----")
        EasyLog.printJson(httpRequest, cacheKey)
        EasyLog.printLog(httpRequest, "----- write cache value -----")
        EasyLog.printJson(httpRequest, cacheValue)
        val writeHttpCacheResult = HttpCacheManager.writeHttpCache(cacheKey, cacheValue)
        EasyLog.printLog(httpRequest, "writeHttpCacheResult = $writeHttpCacheResult")
        val refreshHttpCacheTimeResult = HttpCacheManager.setHttpCacheTime(cacheKey, System.currentTimeMillis())
        EasyLog.printLog(httpRequest, "refreshHttpCacheTimeResult = $refreshHttpCacheTimeResult")
        return writeHttpCacheResult && refreshHttpCacheTimeResult
    }

    override fun clearCache() {
        HttpCacheManager.clearCache()
    }
}