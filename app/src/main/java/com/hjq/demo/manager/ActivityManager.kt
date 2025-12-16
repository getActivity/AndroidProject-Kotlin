package com.hjq.demo.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.hjq.demo.ktx.isAndroid9
import timber.log.Timber
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.nio.charset.StandardCharsets

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/11/18
 *    desc   : Activity 管理类
 */
@Suppress("StaticFieldLeak")
object ActivityManager : ActivityLifecycleCallbacks {

    /** Activity 存放集合 */
    private val activityList: MutableList<Activity> = mutableListOf()

    /** 应用生命周期回调 */
    private val lifecycleCallbacks: MutableList<ApplicationLifecycleCallback> = mutableListOf()

    /** 当前应用上下文对象 */
    private lateinit var application: Application

    /** 栈顶的 Activity 对象 */
    private var topActivity: Activity? = null

    /** 前台并且可见的 Activity 对象 */
    private var resumedActivity: Activity? = null

    fun init(application: Application) {
        this.application = application
        this.application.registerActivityLifecycleCallbacks(this)
    }

    /**
     * 获取 Application 对象
     */
    fun getApplication(): Application {
        return application
    }

    /**
     * 获取栈顶的 Activity
     */
    fun getTopActivity(): Activity? {
        return topActivity
    }

    /**
     * 获取前台并且可见的 Activity
     */
    fun getResumedActivity(): Activity? {
        return resumedActivity
    }

    /**
     * 获取 Activity 集合
     */
    fun getActivityList(): MutableList<Activity> {
        return activityList
    }

    /**
     * 判断当前应用是否处于前台状态
     */
    fun isForeground(): Boolean {
        return getResumedActivity() != null
    }

    /**
     * 注册应用生命周期回调
     */
    fun registerApplicationLifecycleCallback(callback: ApplicationLifecycleCallback) {
        lifecycleCallbacks.add(callback)
    }

    /**
     * 取消注册应用生命周期回调
     */
    fun unregisterApplicationLifecycleCallback(callback: ApplicationLifecycleCallback) {
        lifecycleCallbacks.remove(callback)
    }

    /**
     * 销毁指定的 Activity
     */
    fun finishActivity(clazz: Class<out Activity?>?) {
        if (clazz == null) {
            return
        }

        val iterator: MutableIterator<Activity> = activityList.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            if (activity.javaClass != clazz) {
                continue
            }
            if (!activity.isFinishing) {
                activity.finish()
            }
            iterator.remove()
        }
    }

    /**
     * 销毁所有的 Activity
     */
    fun finishAllActivities() {
        finishAllActivities(null as Class<out Activity?>?)
    }

    /**
     * 销毁所有的 Activity
     *
     * @param classArray            白名单 Activity
     */
    @SafeVarargs
    fun finishAllActivities(vararg classArray: Class<out Activity>?) {
        val iterator: MutableIterator<Activity> = activityList.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            var whiteClazz = false
            for (clazz in classArray) {
                if (activity.javaClass == clazz) {
                    whiteClazz = true
                }
            }
            if (whiteClazz) {
                continue
            }
            // 如果不是白名单上面的 Activity 就销毁掉
            if (!activity.isFinishing) {
                activity.finish()
            }
            iterator.remove()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Timber.i("%s - onCreate", activity.javaClass.simpleName)
        if (activityList.isEmpty()) {
            for (callback: ApplicationLifecycleCallback? in lifecycleCallbacks) {
                callback?.onApplicationCreate(activity)
            }
            Timber.i("%s - onApplicationCreate", activity.javaClass.simpleName)
        }
        activityList.add(activity)
        topActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        Timber.i("%s - onStart", activity.javaClass.simpleName)
    }

    override fun onActivityResumed(activity: Activity) {
        Timber.i("%s - onResume", activity.javaClass.simpleName)
        if (topActivity === activity && resumedActivity == null) {
            for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                callback.onApplicationForeground(activity)
            }
            Timber.i("%s - onApplicationForeground", activity.javaClass.simpleName)
        }
        topActivity = activity
        resumedActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Timber.i("%s - onPause", activity.javaClass.simpleName)
    }

    override fun onActivityStopped(activity: Activity) {
        Timber.i("%s - onStop", activity.javaClass.simpleName)
        if (resumedActivity === activity) {
            resumedActivity = null
        }
        if (resumedActivity == null) {
            for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                callback.onApplicationBackground(activity)
            }
            Timber.i("%s - onApplicationBackground", activity.javaClass.simpleName)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Timber.i("%s - onSaveInstanceState", activity.javaClass.simpleName)
    }

    override fun onActivityDestroyed(activity: Activity) {
        Timber.i("%s - onDestroy", activity.javaClass.simpleName)
        activityList.remove(activity)
        if (topActivity === activity) {
            topActivity = null
        }
        if (activityList.isEmpty()) {
            for (callback: ApplicationLifecycleCallback in lifecycleCallbacks) {
                callback.onApplicationDestroy(activity)
            }
            Timber.i("%s - onApplicationDestroy", activity.javaClass.simpleName)
        }
    }

    /**
     * 判断是否在主进程中
     */
    fun isMainProcess(context: Context): Boolean {
        val processName = getProcessName()
        return if (TextUtils.isEmpty(processName)) {
            // 如果获取不到进程名称，那么则将它当做主进程
            true
        } else TextUtils.equals(processName, context.packageName)
    }

    /**
     * 获取当前进程名称
     */
    @SuppressLint("PrivateApi, DiscouragedPrivateApi")
    fun getProcessName(): String? {
        var processName: String? = null
        if (isAndroid9()) {
            processName = Application.getProcessName()
        } else {
            try {
                val activityThread = Class.forName("android.app.ActivityThread")
                val currentProcessNameMethod =
                    activityThread.getDeclaredMethod("currentProcessName")
                processName = currentProcessNameMethod.invoke(null) as String
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: ClassCastException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
        if (!TextUtils.isEmpty(processName)) {
            return processName
        }

        // 利用 Linux 系统获取进程名
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream("/proc/self/cmdline")
            val buffer = ByteArray(256)
            var len = 0
            var b: Int
            while (inputStream.read().also { b = it } > 0 && len < buffer.size) {
                buffer[len++] = b.toByte()
            }
            if (len > 0) {
                return String(buffer, 0, len, StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * 获取某个对象的唯一标记
     */
    private fun getObjectTag(`object`: Any): String {
        // 对象所在的包名 + 对象的内存地址
        return `object`.javaClass.name + Integer.toHexString(`object`.hashCode())
    }

    /**
     * 应用生命周期回调
     */
    interface ApplicationLifecycleCallback {

        /**
         * 第一个 Activity 创建了
         */
        fun onApplicationCreate(activity: Activity)

        /**
         * 最后一个 Activity 销毁了
         */
        fun onApplicationDestroy(activity: Activity)

        /**
         * 应用从前台进入到后台
         */
        fun onApplicationBackground(activity: Activity)

        /**
         * 应用从后台进入到前台
         */
        fun onApplicationForeground(activity: Activity)
    }
}