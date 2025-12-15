package com.hjq.demo.http.model

import com.hjq.gson.factory.GsonFactory
import com.hjq.http.request.HttpRequest
import com.tencent.mmkv.MMKV

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2022/03/22
 *    desc   : Http 缓存管理器
 */
object HttpCacheManager {

    private val HTTP_CACHE_CONTENT: MMKV = MMKV.mmkvWithID("http_cache_content");

    private val HTTP_CACHE_TIME: MMKV = MMKV.mmkvWithID("http_cache_time")

    /**
     * 生成缓存的 key
     */
    fun generateCacheKey(httpRequest: HttpRequest<*>): String {
        val requestApi = httpRequest.requestApi
        return "请替换成当前的用户 id" + "\n" + requestApi.getApi() + "\n" + GsonFactory.getSingletonGson().toJson(requestApi)
    }

    /**
     * 读取缓存
     */
    fun readHttpCache(cacheKey: String): String? {
        val cacheValue = HTTP_CACHE_CONTENT.getString(cacheKey, null)
        if (cacheValue == null || cacheValue.isEmpty() || "{}" == cacheValue) {
            return null
        }
        return cacheValue
    }

    /**
     * 写入缓存
     */
    fun writeHttpCache(cacheKey: String, cacheValue: String): Boolean {
        return HTTP_CACHE_CONTENT.putString(cacheKey, cacheValue).commit()
    }

    /**
     * 删除缓存
     */
    fun deleteHttpCache(cacheKey: String): Boolean {
        return HTTP_CACHE_CONTENT.remove(cacheKey).commit()
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        HTTP_CACHE_CONTENT.clearMemoryCache()
        HTTP_CACHE_CONTENT.clearAll()

        HTTP_CACHE_TIME.clearMemoryCache()
        HTTP_CACHE_TIME.clearAll()
    }

    /**
     * 获取 Http 写入缓存的时间
     */
    fun getHttpCacheTime(cacheKey: String): Long {
        return HTTP_CACHE_TIME.getLong(cacheKey, 0)
    }

    /**
     * 设置 Http 写入缓存的时间
     */
    fun setHttpCacheTime(cacheKey: String, cacheTime: Long): Boolean {
        return HTTP_CACHE_TIME.putLong(cacheKey, cacheTime).commit()
    }

    /**
     * 判断缓存是否过期
     */
    fun isCacheInvalidate(cacheKey: String, maxCacheTime: Long): Boolean {
        if (maxCacheTime == Long.MAX_VALUE) {
            // 表示缓存长期有效，永远不会过期
            return false
        }
        val httpCacheTime = getHttpCacheTime(cacheKey)
        if (httpCacheTime == 0L) {
            // 表示不知道缓存的时间，这里默认当做已经过期了
            return true
        }
        return httpCacheTime + maxCacheTime < System.currentTimeMillis()
    }
}