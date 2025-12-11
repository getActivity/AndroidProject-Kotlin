package com.hjq.demo.ui.dialog.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.ktx.toast
import com.hjq.widget.layout.SimpleLayout

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/10/09
 *    desc   : 单选或者多选对话框
 */
class SelectDialog {
    class Builder(context: Context) : StyleDialog.Builder<Builder>(context) {

        private val simpleLayout: SimpleLayout? by lazyFindViewById(R.id.sl_select_layout)
        private val recyclerView: RecyclerView? by lazyFindViewById(R.id.rv_select_list)

        private val adapter: SelectAdapter

        private var listener: OnMultiListener<out Any>? = null

        init {
            setCustomView(R.layout.select_dialog)
            recyclerView?.itemAnimator = null
            adapter = SelectAdapter(getContext())
            recyclerView?.adapter = adapter

            simpleLayout?.setMaxHeight(getScreenHeight() / 4 * 3)
        }

        fun setList(vararg ids: Int): Builder = apply {
            val data: MutableList<Any> = ArrayList(ids.size)
            for (id in ids) {
                data.add(getString(id)!!)
            }
            setList(data)
        }

        fun setList(vararg data: String): Builder = apply {
            setList(mutableListOf(*data))
        }

        fun setList(data: MutableList<Any>): Builder = apply {
            adapter.setData(data)
        }

        /**
         * 设置默认选中的位置
         */
        fun setSelect(vararg positions: Int): Builder = apply {
            adapter.setSelect(*positions)
        }

        /**
         * 设置最大选择数量
         */
        fun setMaxSelect(count: Int): Builder = apply {
            adapter.setMaxSelect(count)
        }

        /**
         * 设置最小选择数量
         */
        fun setMinSelect(count: Int): Builder = apply {
            adapter.setMinSelect(count)
        }

        /**
         * 设置单选模式
         */
        fun setSingleSelect(): Builder = apply {
            adapter.setSingleSelect()
        }

        /**
         * 设置多选监听
         */
        @Suppress("UNCHECKED_CAST")
        fun setMultiListener(listener: OnMultiListener<*>?): Builder = apply {
            this.listener = listener as OnMultiListener<out Any>?
        }

        /**
         * 设置单选监听
         */
        @Suppress("UNCHECKED_CAST")
        fun setSingleListener(listener: OnSingleListener<*>?): Builder = apply {
            this.listener = listener as OnSingleListener<out Any>?
        }

        @SingleClick
        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_ui_confirm -> {
                    val data = adapter.getSelectSet()
                    if (data.size >= adapter.getMinSelect()) {
                        performClickDismiss()
                        listener?.onSelfSelected(getDialog(), data)
                    } else {
                        toast(String.format(getString(R.string.select_min_hint)!!, adapter.getMinSelect()))
                    }
                }
                R.id.tv_ui_cancel -> {
                    performClickDismiss()
                    listener?.onCancel(getDialog())
                }
            }
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

    private class SelectAdapter(context: Context) : AppAdapter<Any>(context), BaseAdapter.OnItemClickListener {

        /** 最小选择数量 */
        private var minSelect = 1

        /** 最大选择数量 */
        private var maxSelect = Int.MAX_VALUE

        /** 选择对象集合 */
        private val selectSet: HashMap<Int, Any> = HashMap()

        init {
            setOnItemClickListener(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            return ViewHolder()
        }

        fun setSelect(vararg positions: Int) {
            for (position in positions) {
                selectSet[position] = getItem(position)
            }
            notifyDataSetChanged()
        }

        fun setMaxSelect(count: Int) {
            maxSelect = count
        }

        fun setMinSelect(count: Int) {
            minSelect = count
        }

        fun getMinSelect(): Int {
            return minSelect
        }

        fun setSingleSelect() {
            setMaxSelect(1)
            setMinSelect(1)
        }

        fun isSingleSelect(): Boolean {
            return maxSelect == 1 && minSelect == 1
        }

        fun getSelectSet(): HashMap<Int, Any> {
            return selectSet
        }

        /**
         * [BaseAdapter.OnItemClickListener]
         */
        override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
            if (selectSet.containsKey(position)) {
                // 当前必须不是单选模式才能取消选中
                if (!isSingleSelect()) {
                    selectSet.remove(position)
                    notifyItemChanged(position)
                }
            } else {
                if (maxSelect == 1) {
                    selectSet.clear()
                    notifyDataSetChanged()
                }
                if (selectSet.size < maxSelect) {
                    selectSet[position] = getItem(position)
                    notifyItemChanged(position)
                } else {
                    toast(String.format(getString(R.string.select_max_hint)!!, maxSelect))
                }
            }
        }

        private inner class ViewHolder : AppViewHolder(R.layout.select_item) {

            private val textView: TextView? by lazyFindViewById(R.id.tv_select_text)
            private val checkBox: CheckBox? by lazyFindViewById(R.id.tv_select_checkbox)

            override fun onBindView(position: Int) {
                textView?.text = getItem(position).toString()
                checkBox?.isChecked = selectSet.containsKey(position)
                if (maxSelect == 1) {
                    checkBox?.isClickable = false
                } else {
                    checkBox?.isEnabled = false
                }
            }
        }
    }

    interface OnMultiListener<T> {

        /**
         * 选择回调
         *
         * @param data              选择的位置和数据
         */
        @Suppress("UNCHECKED_CAST")
        fun onSelfSelected(dialog: BaseDialog?, data: HashMap<Int, out Any>) {
            onSelected(dialog, data as HashMap<Int, T>)
        }

        /**
         * 选择回调
         *
         * @param data              选择的位置和数据
         */
        fun onSelected(dialog: BaseDialog?, data: HashMap<Int, T>)

        /**
         * 取消回调
         */
        fun onCancel(dialog: BaseDialog?) {}
    }

    interface OnSingleListener<T> : OnMultiListener<T> {

        override fun onSelected(dialog: BaseDialog?, data: HashMap<Int, T>) {
            val keys: Set<Int> = data.keys
            val iterator = keys.iterator()
            if (!iterator.hasNext()) {
                return
            }
            val key = iterator.next()
            onSelected(dialog, key, data[key])
        }

        /**
         * 选择回调
         *
         * @param position          选择的位置
         * @param data              选择的数据
         */
        fun onSelected(dialog: BaseDialog?, position: Int, data: T?)
    }
}