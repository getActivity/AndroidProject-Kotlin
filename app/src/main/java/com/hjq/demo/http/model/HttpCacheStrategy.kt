package com.hjq.demo.http.model

import com.hjq.gson.factory.GsonFactory
import com.hjq.http.EasyLog
import com.hjq.http.config.IHttpCacheStrategy
import com.hjq.http.request.HttpRequest
import okhttp3.Response
import java.lang.reflect.Type

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2025/03/23
 *    desc   : 请求缓存策略实现类
 */
class HttpCacheStrategy : IHttpCacheStrategy {

    override fun readCache(httpRequest: HttpRequest<*>, type: Type, cacheTime: Long): Any? {
        val cacheKey = HttpCacheManager.generateCacheKey(httpRequest)
        val cacheValue = HttpCacheManager.readHttpCache(cacheKey)
        if (cacheValue == null || cacheValue.isEmpty() || "{}" == cacheValue) {
            return null
        }
        EasyLog.printLog(httpRequest, "----- read cache key -----")
        EasyLog.printJson(httpRequest, cacheKey)
        EasyLog.printLog(httpRequest, "----- read cache value -----")
        EasyLog.printJson(httpRequest, cacheValue)
        EasyLog.printLog(httpRequest, "cacheTime = $cacheTime")
        val cacheInvalidate = HttpCacheManager.isCacheInvalidate(cacheKey, cacheTime)
        EasyLog.printLog(httpRequest, "cacheInvalidate = $cacheInvalidate")
        if (cacheInvalidate) {
            // 表示缓存已经过期了，直接返回 null 给外层，表示缓存不可用
            return null
        }
        return GsonFactory.getSingletonGson().fromJson<Any?>(cacheValue, type)
    }

    override fun writeCache(httpRequest: HttpRequest<*>, response: Response, result: Any): Boolean {
        val cacheKey = HttpCacheManager.generateCacheKey(httpRequest)
        val cacheValue = GsonFactory.getSingletonGson().toJson(result)
        if (cacheValue == null || cacheValue.isEmpty() || "{}" == cacheValue) {
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

    override fun deleteCache(httpRequest: HttpRequest<*>): Boolean {
        val cacheKey = HttpCacheManager.generateCacheKey(httpRequest)
        EasyLog.printLog(httpRequest, "----- delete cache key -----")
        EasyLog.printJson(httpRequest, cacheKey)
        val deleteHttpCacheResult = HttpCacheManager.deleteHttpCache(cacheKey)
        EasyLog.printLog(httpRequest, "deleteHttpCacheResult = $deleteHttpCacheResult")
        return deleteHttpCacheResult
    }

    override fun clearCache() {
        HttpCacheManager.clearCache()
    }
}