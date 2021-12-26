package com.hjq.demo.manager

import android.content.Context
import android.os.Environment
import com.tencent.bugly.crashreport.CrashReport
import java.io.File
import java.math.BigDecimal

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/03/01
 *    desc   : 应用缓存管理
 */
object CacheDataManager {

    /**
     * 获取缓存大小
     */
    fun getTotalCacheSize(context: Context): String {
        var cacheSize: Long = getFolderSize(context.cacheDir)
        if ((Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.externalCacheDir!!)
        }
        return getFormatSize(cacheSize.toDouble())
    }

    /**
     * 清除缓存
     */
    fun clearAllCache(context: Context) {
        deleteDir(context.cacheDir)
        if ((Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)) {
            deleteDir(context.externalCacheDir)
        }
    }

    /**
     * 删除文件夹
     */
    private fun deleteDir(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        if (!dir.isDirectory) {
            return dir.delete()
        }
        val children: Array<out String> = dir.list() ?: return false
        for (child: String in children) {
            deleteDir(File(dir, child))
        }
        return false
    }

    // 获取文件大小
    // Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    // Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val list: Array<out File> = file.listFiles() ?: return 0
            for (temp: File in list) {
                // 如果下面还有文件
                size += if (temp.isDirectory) {
                    getFolderSize(temp)
                } else {
                    temp.length()
                }
            }
        } catch (e: Exception) {
            CrashReport.postCatchedException(e)
        }
        return size
    }

    /**
     * 格式化单位
     */
    fun getFormatSize(size: Double): String {
        val kiloByte: Double = size / 1024
        if (kiloByte < 1) {
            // return size + "Byte";
            return "0K"
        }
        val megaByte: Double = kiloByte / 1024
        if (megaByte < 1) {
            return BigDecimal(kiloByte).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K"
        }
        val gigaByte: Double = megaByte / 1024
        if (gigaByte < 1) {
            return BigDecimal(megaByte).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M"
        }
        val teraBytes: Double = gigaByte / 1024
        if (teraBytes < 1) {
            return BigDecimal(gigaByte).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        }
        return BigDecimal(teraBytes).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }
}