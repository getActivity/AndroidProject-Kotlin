package com.hjq.demo.ui.adapter

import android.content.Context
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.http.glide.GlideApp

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/08/28
 *    desc   : 图片预览适配器
 */
class ImagePreviewAdapter constructor(context: Context) : AppAdapter<String?>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder()
    }

    inner class ViewHolder : AppViewHolder(R.layout.image_preview_item) {

        private val photoView: PhotoView by lazy { getItemView() as PhotoView }

        override fun onBindView(position: Int) {
            GlideApp.with(getContext())
                .load(getItem(position))
                .into(photoView)
        }
    }
}