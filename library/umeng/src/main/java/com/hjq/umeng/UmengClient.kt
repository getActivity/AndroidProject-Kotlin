package com.hjq.umeng

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.hjq.umeng.UmengLogin.LoginListenerWrapper
import com.hjq.umeng.UmengLogin.OnLoginListener
import com.hjq.umeng.UmengShare.OnShareListener
import com.hjq.umeng.UmengShare.ShareListenerWrapper
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareAPI

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/04/03
 *    desc   : 友盟客户端
 */
object UmengClient {

    private var deviceOaid: String? = null

    /**
     * 初始化友盟相关 SDK
     */
    fun init(application: Application?, logEnable: Boolean) {
        preInit(application, logEnable)
        // 友盟统计：https://developer.umeng.com/docs/66632/detail/101814#h1-u521Du59CBu5316u53CAu901Au7528u63A5u53E32
        UMConfigure.init(application, BuildConfig.UM_KEY, "umeng", UMConfigure.DEVICE_TYPE_PHONE, "")
        // 获取设备的 oaid
        UMConfigure.getOaid(application) { oaid: String? -> deviceOaid = oaid }
    }

    /**
     * 预初始化 SDK（在用户没有同意隐私协议前调用）
     */
    fun preInit(application: Application?, logEnable: Boolean) {
        UMConfigure.preInit(application, BuildConfig.UM_KEY, "umeng")
        // 选用自动采集模式：https://developer.umeng.com/docs/119267/detail/118588#h1-u9875u9762u91C7u96C63
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)

        // 初始化各个平台的 ID 和 Key
        PlatformConfig.setWeixin(BuildConfig.WX_ID, BuildConfig.WX_SECRET)
        PlatformConfig.setQQZone(BuildConfig.QQ_ID, BuildConfig.QQ_SECRET)

        // 初始化各个平台的文件提供者（必须要初始化，否则会导致无法分享文件）
        val fileProvider = application?.packageName + ".provider"
        PlatformConfig.setWXFileProvider(fileProvider)
        PlatformConfig.setQQFileProvider(fileProvider)

        // 是否开启日志打印
        UMConfigure.setLogEnabled(logEnable)
    }

    /**
     * 分享
     *
     * @param activity              Activity对象
     * @param platform              分享平台
     * @param action                分享意图
     * @param listener              分享监听
     */
    fun share(activity: Activity?, platform: Platform?, action: ShareAction?, listener: OnShareListener?) {
        if (platform == null) {
            return
        }
        if (action == null) {
            return
        }
        if (!isAppInstalled(activity, platform.getPackageName())) {
            // 当分享的平台软件可能没有被安装的时候
            if (listener == null) {
                return
            }
            listener.onError(platform, PackageManager.NameNotFoundException("Is not installed"))
            return
        }
        action.setPlatform(platform.getThirdParty())
            .setCallback(ShareListenerWrapper(platform.getThirdParty()!!, listener))
            .share()
    }

    /**
     * 登录
     *
     * @param activity              Activity对象
     * @param platform              登录平台
     * @param listener              登录监听
     */
    fun login(activity: Activity?, platform: Platform?, listener: OnLoginListener?) {
        if (platform == null) {
            return
        }
        if (!isAppInstalled(activity, platform)) {
            // 当登录的平台软件可能没有被安装的时候
            if (listener == null) {
                return
            }
            listener.onError(platform, PackageManager.NameNotFoundException("Is not installed"))
            return
        }
        try {
            // 删除旧的第三方登录授权
            UMShareAPI.get(activity).deleteOauth(activity, platform.getThirdParty(), null)
            // 要先等上面的代码执行完毕之后
            Thread.sleep(200)
            // 开启新的第三方登录授权
            UMShareAPI.get(activity).getPlatformInfo(
                activity,
                platform.getThirdParty(),
                LoginListenerWrapper(platform.getThirdParty()!!, listener)
            )
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 设置回调
     */
    fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 获取设备 oaid
     */
    fun getDeviceOaid(): String? {
        return deviceOaid
    }

    /**
     * 判断 App 是否安装
     */
    fun isAppInstalled(context: Context?, platform: Platform?): Boolean {
        return isAppInstalled(context, platform?.getPackageName())
    }

    private fun isAppInstalled(context: Context?, packageName: String?): Boolean {
        if (packageName == null || "" == packageName) {
            return false
        }
        return try {
            context?.packageManager?.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}