package com.hjq.umeng

import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.bean.SHARE_MEDIA

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/04/03
 *    desc   : 友盟第三方登录
 */
class UmengLogin {

    /**
     * 第三方登录获取用户资料：https://developer.umeng.com/docs/66632/detail/66639#h3-u83B7u53D6u7528u6237u8D44u6599
     */
    class LoginData internal constructor(data: MutableMap<String?, String?>?) {

        /** 用户 id */
        private val id: String? = data?.get("uid")

        /** 昵称 */
        private val name: String? = data?.get("name")

        /** 性别 */
        private val sex: String? = data?.get("gender")

        /** 头像 */
        private val avatar: String? = data?.get("iconurl")

        /** Token */
        private val token: String? = data?.get("accessToken")

        fun getName(): String? {
            return name
        }

        fun getSex(): String? {
            return sex
        }

        fun getAvatar(): String? {
            return avatar
        }

        fun getId(): String? {
            return id
        }

        fun getToken(): String? {
            return token
        }

        /**
         * 判断当前的性别是否为男性
         */
        fun isMan(): Boolean {
            return ("男" == sex)
        }
    }

    /**
     * 为什么要包起来？因为友盟会将监听回调（UMAuthListener）持有成静态的，回调完没有及时释放
     */
    class LoginListenerWrapper internal constructor(platform: SHARE_MEDIA, private var listener: OnLoginListener?) : UMAuthListener {

        private var platform: Platform = when (platform) {
            SHARE_MEDIA.QQ -> Platform.QQ
            SHARE_MEDIA.WEIXIN -> Platform.WECHAT
            else -> throw IllegalStateException("are you ok?")
        }

        /**
         * 授权开始的回调
         *
         * @param platform      平台名称
         */
        override fun onStart(platform: SHARE_MEDIA) {
            listener?.onStart(this.platform)
        }

        /**
         * 授权成功的回调
         *
         * @param platform      平台名称
         * @param action        行为序号，开发者用不上
         * @param data          用户资料返回
         */
        override fun onComplete(platform: SHARE_MEDIA, action: Int, data: MutableMap<String?, String?>?) {
            listener?.onSucceed(this.platform, LoginData(data))
            listener = null
        }

        /**
         * 授权失败的回调
         *
         * @param platform      平台名称
         * @param action        行为序号，开发者用不上
         * @param t             错误原因
         */
        override fun onError(platform: SHARE_MEDIA?, action: Int, t: Throwable) {
            t.printStackTrace()
            listener?.onError(this.platform, t)
            listener = null
        }

        /**
         * 授权取消的回调
         *
         * @param platform      平台名称
         * @param action        行为序号，开发者用不上
         */
        override fun onCancel(platform: SHARE_MEDIA?, action: Int) {
            listener?.onCancel(this.platform)
            listener = null
        }
    }

    interface OnLoginListener {

        /**
         * 授权开始
         *
         * @param platform      平台对象
         */
        fun onStart(platform: Platform?) {}

        /**
         * 授权成功的回调
         *
         * @param platform      平台对象
         * @param data          用户资料返回
         */
        fun onSucceed(platform: Platform?, data: LoginData?)

        /**
         * 授权失败的回调
         *
         * @param platform      平台对象
         * @param t             错误原因
         */
        fun onError(platform: Platform?, t: Throwable) {}

        /**
         * 授权取消的回调
         *
         * @param platform      平台对象
         */
        fun onCancel(platform: Platform?) {}
    }
}