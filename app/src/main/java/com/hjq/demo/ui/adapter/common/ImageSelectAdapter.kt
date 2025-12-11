package com.hjq.demo.ui.adapter.common

import android.content.Context
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.http.glide.GlideApp

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/07/24
 *    desc   : 图片选择适配器
 */
class ImageSelectAdapter constructor(context: Context, private val selectImages: MutableList<String>) : AppAdapter<String>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return ViewHolder()
    }

    inner class ViewHolder : AppViewHolder(R.layout.image_select_item) {

        private val imageView: ImageView? by lazyFindViewById(R.id.iv_image_select_image)
        private val checkBox: CheckBox? by lazyFindViewById(R.id.cb_image_select_check)

        override fun onBindView(position: Int) {
            getItem(position).apply {
                imageView?.let {
                    GlideApp.with(getContext())
                        .asBitmap()
                        .load(this)
                        .into(it)
                }
                checkBox?.isChecked = selectImages.contains(this)
            }
        }
    }

    override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 3)
    }
}