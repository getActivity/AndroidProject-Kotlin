package com.hjq.demo.ui.dialog

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter
import com.hjq.toast.ToastUtils
import com.hjq.umeng.Platform
import com.hjq.umeng.UmengClient
import com.hjq.umeng.UmengShare.OnShareListener
import com.umeng.socialize.ShareAction
import com.umeng.socialize.ShareContent
import com.umeng.socialize.media.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/03/23
 *    desc   : 分享对话框
 */
class ShareDialog {

    class Builder(activity: Activity) : BaseDialog.Builder<Builder>(activity), BaseAdapter.OnItemClickListener {

        private val recyclerView: RecyclerView? by lazy { findViewById(R.id.rv_share_list) }

        private val adapter: ShareAdapter
        private val shareAction: ShareAction
        private val copyLink: ShareBean
        private var listener: OnShareListener? = null

        init {
            setContentView(R.layout.share_dialog)
            val data: MutableList<ShareBean> = ArrayList()
            data.add(ShareBean(getDrawable(R.drawable.share_wechat_ic)!!, getString(R.string.share_platform_wechat)!!, Platform.WECHAT))
            data.add(ShareBean(getDrawable(R.drawable.share_moment_ic)!!, getString(R.string.share_platform_moment)!!, Platform.CIRCLE))
            data.add(ShareBean(getDrawable(R.drawable.share_qq_ic)!!, getString(R.string.share_platform_qq)!!, Platform.QQ))
            data.add(ShareBean(getDrawable(R.drawable.share_qzone_ic)!!, getString(R.string.share_platform_qzone)!!, Platform.QZONE))
            copyLink = ShareBean(getDrawable(R.drawable.share_link_ic)!!, getString(R.string.share_platform_link)!!, null)
            adapter = ShareAdapter(activity)
            adapter.setData(data)
            adapter.setOnItemClickListener(this)
            recyclerView?.layoutManager = GridLayoutManager(activity, data.size)
            recyclerView?.adapter = adapter
            shareAction = ShareAction(activity)
        }

        /**
         * 分享网页链接：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu7F51u9875u94FEu63A51
         */
        fun setShareLink(content: UMWeb?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 分享图片：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu56FEu72473
         */
        fun setShareImage(content: UMImage?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 分享纯文本：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu7EAFu6587u672C5
         */
        fun setShareText(content: String?): Builder = apply {
            shareAction.withText(content)
            refreshShareOptions()
        }

        /**
         * 分享音乐：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu97F3u4E507
         */
        fun setShareMusic(content: UMusic?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 分享视频：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu89C6u98916
         */
        fun setShareVideo(content: UMVideo?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 分享 Gif 表情：https://developer.umeng.com/docs/128606/detail/193883#h2--gif-8
         */
        fun setShareEmoji(content: UMEmoji?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 分享微信小程序：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu5C0Fu7A0Bu5E8F2
         */
        fun setShareMin(content: UMMin?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 分享 QQ 小程序：https://developer.umeng.com/docs/128606/detail/193883#h2-u5206u4EABu5C0Fu7A0Bu5E8F2
         */
        fun setShareMin(content: UMQQMini?): Builder = apply {
            shareAction.withMedia(content)
            refreshShareOptions()
        }

        /**
         * 设置回调监听器
         */
        fun setListener(listener: OnShareListener?): Builder = apply {
            this.listener = listener
        }

        /**
         * [BaseAdapter.OnItemClickListener]
         */
        override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
            val platform = adapter.getItem(position).sharePlatform
            if (platform != null) {
                if (getContext().packageName.endsWith(".debug") &&
                    (platform === Platform.WECHAT || platform === Platform.CIRCLE)) {
                    ToastUtils.show("当前 buildType 不支持进行微信分享")
                    return
                }
                UmengClient.share(getActivity(), platform, shareAction, listener)
            } else {
                if (shareAction.shareContent.shareType == ShareContent.WEB_STYLE) {
                    // 复制到剪贴板
                    getSystemService(ClipboardManager::class.java).setPrimaryClip(
                        ClipData.newPlainText("url", shareAction.shareContent.mMedia.toUrl()))
                    ToastUtils.show(R.string.share_platform_copy_hint)
                }
            }
            dismiss()
        }

        /**
         * 刷新分享选项
         */
        private fun refreshShareOptions() {
            when (shareAction.shareContent.shareType) {
                ShareContent.WEB_STYLE -> if (!adapter.containsItem(copyLink)) {
                    adapter.addItem(copyLink)
                    recyclerView?.layoutManager = GridLayoutManager(getContext(), adapter.getCount())
                }
                else -> if (adapter.containsItem(copyLink)) {
                    adapter.removeItem(copyLink)
                    recyclerView?.layoutManager = GridLayoutManager(getContext(), adapter.getCount())
                }
            }
        }
    }

    private class ShareAdapter(context: Context) : AppAdapter<ShareBean>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder()
        }

        private inner class ViewHolder : AppViewHolder(R.layout.share_item) {

            private val imageView: ImageView? by lazy { findViewById(R.id.iv_share_image) }
            private val textView: TextView? by lazy { findViewById(R.id.tv_share_text) }

            override fun onBindView(position: Int) {
                getItem(position).apply {
                    imageView?.setImageDrawable(shareIcon)
                    textView?.text = shareName
                }
            }
        }
    }

    class ShareBean (

        /** 分享图标 */
        val shareIcon: Drawable,
        /** 分享名称 */
        val shareName: String,
        /** 分享平台 */
        val sharePlatform: Platform?
    )
}