package com.hjq.umeng

import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/04/03
 *    desc   : 友盟第三方分享
 */
class UmengShare {

    /**
     * 为什么要包起来？因为友盟会将监听回调（UMShareListener）持有成静态的，回调完没有及时释放
     */
    class ShareListenerWrapper internal constructor(
        platform: SHARE_MEDIA,
        private var listener: OnShareListener?) : UMShareListener {

        private var platform: Platform = when (platform) {
            SHARE_MEDIA.QQ -> Platform.QQ
            SHARE_MEDIA.QZONE -> Platform.QZONE
            SHARE_MEDIA.WEIXIN -> Platform.WECHAT
            SHARE_MEDIA.WEIXIN_CIRCLE -> Platform.CIRCLE
            else -> throw IllegalStateException("are you ok?")
        }

        /**
         * 分享开始的回调
         *
         * @param platform      平台名称
         */
        override fun onStart(platform: SHARE_MEDIA) {
            listener?.onStart(this.platform)
        }

        /**
         * 分享成功的回调
         *
         * @param platform      平台名称
         */
        override fun onResult(platform: SHARE_MEDIA) {
            listener?.onSucceed(this.platform)
            listener = null
        }

        /**
         * 分享失败的回调
         *
         * @param platform      平台名称
         * @param t             错误原因
         */
        override fun onError(platform: SHARE_MEDIA, t: Throwable) {
            t.printStackTrace()
            listener?.onError(this.platform, t)
            listener = null
        }

        /**
         * 分享取消的回调
         *
         * @param platform      平台名称
         */
        override fun onCancel(platform: SHARE_MEDIA) {
            listener?.onCancel(this.platform)
            listener = null
        }
    }

    interface OnShareListener {

        /**
         * 分享开始
         *
         * @param platform      平台对象
         */
        fun onStart(platform: Platform?) {}

        /**
         * 分享成功的回调
         *
         * @param platform      平台对象
         */
        fun onSucceed(platform: Platform?)

        /**
         * 分享失败的回调
         *
         * @param platform      平台对象
         * @param t             错误原因
         */
        fun onError(platform: Platform?, t: Throwable) {}

        /**
         * 分享取消的回调
         *
         * @param platform      平台对象
         */
        fun onCancel(platform: Platform?) {}
    }
}