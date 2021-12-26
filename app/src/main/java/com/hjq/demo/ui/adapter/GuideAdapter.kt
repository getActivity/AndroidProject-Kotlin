package com.hjq.demo.ui.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/08/28
 *    desc   : 引导页适配器
 */
class GuideAdapter constructor(context: Context) : AppAdapter<Int>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder()
    }

    inner class ViewHolder : AppViewHolder(R.layout.guide_item) {

        private val imageView: ImageView by lazy { getItemView() as ImageView }

        override fun onBindView(position: Int) {
            imageView.setImageResource(getItem(position))
        }
    }
}