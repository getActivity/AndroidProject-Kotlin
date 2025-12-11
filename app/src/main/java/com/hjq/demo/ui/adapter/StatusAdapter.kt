package com.hjq.demo.ui.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/22
 *    desc   : 状态数据列表
 */
class StatusAdapter constructor(context: Context) : AppAdapter<String?>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return ViewHolder()
    }

    inner class ViewHolder : AppViewHolder(R.layout.status_item) {

        private val textView: TextView? by lazyFindViewById(R.id.tv_status_text)

        override fun onBindView(position: Int) {
            textView?.text = getItem(position)
        }
    }
}