package com.hjq.demo.ui.dialog.common

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.core.action.AnimAction
import com.hjq.custom.widget.layout.SimpleLayout
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/2
 *    desc   : 菜单选择框
 */
class MenuDialog {

    class Builder(context: Context) : BaseDialog.Builder<Builder>(context),
        BaseAdapter.OnItemClickListener {

        private val simpleLayout: SimpleLayout? by lazyFindViewById(R.id.sl_menu_layout)
        private val recyclerView: RecyclerView? by lazyFindViewById(R.id.rv_menu_list)
        private val cancelView: TextView? by lazyFindViewById(R.id.tv_menu_cancel)

        private val adapter: MenuAdapter
        private var autoDismiss = true

        private var listener: OnListener<Any>? = null

        init {
            setContentView(R.layout.menu_dialog)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setOnClickListener(cancelView)
            adapter = MenuAdapter(getContext())
            adapter.setOnItemClickListener(this)
            recyclerView?.adapter = adapter

            simpleLayout?.setMaxHeight(getScreenHeight() / 4 * 3)
        }

        override fun setGravity(gravity: Int): Builder = apply {
            when (gravity) {
                Gravity.CENTER, Gravity.CENTER_VERTICAL -> {
                    // 不显示取消按钮
                    setCancel(null)
                    // 重新设置动画
                    setAnimStyle(AnimAction.ANIM_SCALE)
                }
            }
            super.setGravity(gravity)
        }

        fun setList(vararg ids: Int): Builder = apply {
            val data: MutableList<Any> = mutableListOf()
            for (id in ids) {
                getString(id)?.let {
                    data.add(it)
                }
            }
            setList(data)
        }

        fun setList(vararg data: String): Builder = apply {
            setList(mutableListOf(*data))
        }

        @Suppress("UNCHECKED_CAST")
        fun setList(data: MutableList<out Any>): Builder = apply {
            adapter.setData(data as MutableList<Any>)
        }

        fun setCancel(@StringRes id: Int): Builder = apply {
            setCancel(getString(id))
        }

        fun setCancel(text: CharSequence?): Builder = apply {
            cancelView?.text = text
        }

        fun setAutoDismiss(dismiss: Boolean): Builder = apply {
            autoDismiss = dismiss
        }

        @Suppress("UNCHECKED_CAST")
        fun setListener(listener: OnListener<out Any>?): Builder = apply {
            this.listener = listener as OnListener<Any>?
        }

        @SingleClick
        override fun onClick(view: View) {
            if (autoDismiss) {
                dismiss()
            }
            if (view === cancelView) {
                listener?.onCancel(requireNotNull(getDialog()))
            }
        }

        /**
         * [BaseAdapter.OnItemClickListener]
         */
        override fun onItemClick(recyclerView: RecyclerView, itemView: View, position: Int) {
            if (autoDismiss) {
                dismiss()
            }
            listener?.onSelected(requireNotNull(getDialog()), position, adapter.getItem(position))
        }

        /**
         * 获取屏幕的高度
         */
        private fun getScreenHeight(): Int {
            val resources = getResources()
            val outMetrics = resources.displayMetrics
            return outMetrics.heightPixels
        }
    }

    class MenuAdapter (context: Context) : AppAdapter<Any>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            return ViewHolder()
        }

        inner class ViewHolder : AppViewHolder(R.layout.menu_item) {

            private val textView: TextView? by lazyFindViewById(R.id.tv_menu_text)
            private val lineView: View? by lazyFindViewById(R.id.v_menu_line)

            override fun onBindView(position: Int) {
                textView?.text = getItem(position).toString()
                if (position == 0) {
                    // 当前是否只有一个条目
                    if (getCount() == 1) {
                        lineView?.visibility = View.GONE
                    } else {
                        lineView?.visibility = View.VISIBLE
                    }
                } else if (position == getCount() - 1) {
                    lineView?.visibility = View.GONE
                } else {
                    lineView?.visibility = View.VISIBLE
                }
            }
        }
    }

    interface OnListener<T> {

        /**
         * 选择条目时回调
         */
        fun onSelected(dialog: BaseDialog, position: Int, data: T)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog) {
            // default implementation ignored
        }
    }
}