package com.hjq.demo.ui.dialog.common

import android.content.Context
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick


/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/2
 *    desc   : 消息对话框
 */
class MessageDialog {

    open class Builder constructor(context: Context) : StyleDialog.Builder<Builder>(context) {

        val messageView: TextView? by lazyFindViewById(R.id.tv_message_message)

        private var listener: OnListener? = null

        init {
            setCustomView(R.layout.message_dialog)

            // 让 TextView 支持滚动
            messageView?.movementMethod = ScrollingMovementMethod()
        }

        fun setMessage(@StringRes id: Int): Builder = apply {
            setMessage(getString(id))
        }

        fun setMessage(text: CharSequence?): Builder = apply {
            messageView?.text = text
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        override fun create(): BaseDialog {
            // 如果内容为空就抛出异常
            if (TextUtils.isEmpty(messageView?.text.toString())) {
                throw IllegalArgumentException("Dialog message not null")
            }
            return super.create()
        }

        @SingleClick
        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_ui_confirm -> {
                    performClickDismiss()
                    listener?.onConfirm(getDialog())
                }
                R.id.tv_ui_cancel -> {
                    performClickDismiss()
                    listener?.onCancel(getDialog())
                }
            }
        }
    }

    interface OnListener {

        /**
         * 点击确定时回调
         */
        fun onConfirm(dialog: BaseDialog?)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog?) {}
    }
}