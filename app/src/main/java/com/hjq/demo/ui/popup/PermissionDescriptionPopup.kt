package com.hjq.demo.ui.popup

import android.content.Context
import android.view.WindowManager
import android.widget.TextView
import com.hjq.base.BasePopupWindow
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2025/06/01
 *    desc   : 权限说明弹窗
 */
class PermissionDescriptionPopup {

    class Builder(context: Context) : BasePopupWindow.Builder<Builder>(context) {

        private val descriptionView: TextView? by lazyFindViewById(R.id.tv_permission_description_message)

        init {
            setContentView(R.layout.permission_description_popup)
            setWidth(WindowManager.LayoutParams.MATCH_PARENT)
            setHeight(WindowManager.LayoutParams.WRAP_CONTENT)
            setAnimStyle(android.R.style.Animation_Dialog)
            setBackgroundDimAmount(0.1f)
            setTouchable(true)
            setOutsideTouchable(true)
        }

        /**
         * 设置权限说明文案
         */
        fun setDescription(text: CharSequence?) = apply {
            descriptionView?.text = text
        }
    }
}