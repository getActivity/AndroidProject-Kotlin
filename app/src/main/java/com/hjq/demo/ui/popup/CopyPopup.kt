package com.hjq.demo.ui.popup

import android.content.Context
import com.hjq.base.BasePopupWindow
import com.hjq.demo.R


/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/10/18
 *    desc   : 可进行拷贝的副本
 */
class CopyPopup {

    class Builder(context: Context) : BasePopupWindow.Builder<Builder>(context) {

        init {
            setContentView(R.layout.copy_popup)
        }
    }
}