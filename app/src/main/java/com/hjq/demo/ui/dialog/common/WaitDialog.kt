package com.hjq.demo.ui.dialog.common

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.hjq.base.BaseDialog
import com.hjq.base.action.AnimAction
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/2
 *    desc   : 等待加载对话框
 */
class WaitDialog {

    class Builder(context: Context) : BaseDialog.Builder<Builder>(context) {

        private val messageView: TextView? by lazyFindViewById(R.id.tv_wait_message)

        init {
            setContentView(R.layout.wait_dialog)
            setAnimStyle(AnimAction.ANIM_TOAST)
            setBackgroundDimEnabled(false)
            setCancelable(false)
        }

        fun setMessage(@StringRes id: Int): Builder = apply {
            setMessage(getString(id))
        }

        fun setMessage(text: CharSequence?): Builder = apply {
            messageView?.text = text
            messageView?.visibility = if (text == null) View.GONE else View.VISIBLE
        }
    }
}