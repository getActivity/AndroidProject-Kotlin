package com.hjq.demo.ui.dialog.common

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.hjq.base.BaseDialog
import com.hjq.base.action.AnimAction
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/21
 *    desc   : 项目通用样式 Dialog 布局封装
 */
class StyleDialog {

    @Suppress("UNCHECKED_CAST", "LeakingThis")
    open class Builder<B : Builder<B>>(context: Context) : BaseDialog.Builder<B>(context) {

        private var clickDismiss = true

        private val containerLayout: ViewGroup? by lazyFindViewById(R.id.ll_ui_container)
        private val titleView: TextView? by lazyFindViewById(R.id.tv_ui_title)
        private val cancelView: TextView? by lazyFindViewById(R.id.tv_ui_cancel)
        private val lineView: View? by lazyFindViewById(R.id.v_ui_line)
        private val confirmView: TextView? by lazyFindViewById(R.id.tv_ui_confirm)

        init {
            setContentView(R.layout.ui_dialog)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setOnClickListener(cancelView, confirmView)
        }

        fun setCustomView(@LayoutRes id: Int): B {
            return setCustomView(LayoutInflater.from(getContext()).inflate(id, containerLayout, false))
        }

        fun setCustomView(view: View?): B {
            containerLayout?.addView(view, 1)
            return this as B
        }

        fun setTitle(@StringRes id: Int): B {
            return setTitle(getString(id))
        }

        fun setTitle(text: CharSequence?): B {
            titleView?.text = text
            return this as B
        }

        fun setCancel(@StringRes id: Int): B {
            return setCancel(getString(id))
        }

        fun setCancel(text: CharSequence?): B {
            cancelView?.text = text
            lineView?.visibility = if (text == null || "" == text.toString()) View.GONE else View.VISIBLE
            return this as B
        }

        fun setConfirm(@StringRes id: Int): B {
            return setConfirm(getString(id))
        }

        fun setConfirm(text: CharSequence?): B {
            confirmView?.text = text
            return this as B
        }

        fun setClickDismiss(enable: Boolean): B {
            clickDismiss = enable
            return this as B
        }

        fun performClickDismiss() {
            if (!clickDismiss) {
                return
            }
            dismiss()
        }
    }
}