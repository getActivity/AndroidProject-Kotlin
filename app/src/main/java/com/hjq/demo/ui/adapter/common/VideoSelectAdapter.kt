package com.hjq.demo.ui.adapter.common

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.core.manager.CacheDataManager
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.ui.activity.common.VideoSelectActivity
import com.hjq.demo.widget.PlayerView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/03/01
 *    desc   : 视频选择适配器
 */
class VideoSelectAdapter(context: Context, images: MutableList<VideoSelectActivity.VideoBean>) :
                         AppAdapter<VideoSelectActivity.VideoBean>(context) {

    private val selectVideo: MutableList<VideoSelectActivity.VideoBean> = images

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return ViewHolder()
    }

    inner class ViewHolder : AppViewHolder(R.layout.video_select_item) {

        private val imageView: ImageView? by lazyFindViewById(R.id.iv_video_select_image)
        private val checkBox: CheckBox? by lazyFindViewById(R.id.iv_video_select_check)
        private val durationView: TextView? by lazyFindViewById(R.id.tv_video_select_duration)
        private val sizeView: TextView? by lazyFindViewById(R.id.tv_video_select_size)

        override fun onBindView(position: Int) {
            getItem(position).apply {

                imageView?.let {
                    GlideApp.with(getContext())
                        .load(getVideoPath())
                        .into(it)
                }

                checkBox?.isChecked = selectVideo.contains(getItem(position))

                // 获取视频的总时长
                durationView?.text = PlayerView.conversionTime(getVideoDuration().toInt())

                // 获取视频文件大小
                sizeView?.text = CacheDataManager.getFormatSize(getVideoSize().toDouble())
            }
        }
    }

    override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 2)
    }
}