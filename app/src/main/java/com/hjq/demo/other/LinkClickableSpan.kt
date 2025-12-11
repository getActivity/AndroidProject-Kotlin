package com.hjq.demo.other

import android.text.style.ClickableSpan
import android.view.View
import com.hjq.demo.ui.activity.common.BrowserActivity

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2023/06/24
 *    desc   : 点击跳转链接的 ClickableSpan
 */
class LinkClickableSpan(private val targetUrl: String?) : ClickableSpan() {

    override fun onClick(widget: View) {
        targetUrl?.let {
            BrowserActivity.start(widget.context, it)
        }
    }
}