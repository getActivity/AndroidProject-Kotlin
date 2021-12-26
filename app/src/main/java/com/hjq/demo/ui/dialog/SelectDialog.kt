package com.hjq.demo.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter
import com.hjq.toast.ToastUtils

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/10/09
 *    desc   : 单选或者多选对话框
 */
class SelectDialog {
    class Builder(context: Context) : CommonDialog.Builder<Builder>(context),
        View.OnLayoutChangeListener, Runnable {

        private val recyclerView: RecyclerView? by lazy { findViewById(R.id.rv_select_list) }

        private val adapter: SelectAdapter

        private var listener: OnListener<out Any>? = null

        init {
            setCustomView(R.layout.select_dialog)
            recyclerView?.itemAnimator = null
            adapter = SelectAdapter(getContext())
            recyclerView?.adapter = adapter
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
            recyclerView?.addOnLayoutChangeListener(this)
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

        @Suppress("UNCHECKED_CAST")
        fun setListener(listener: OnListener<*>?): Builder = apply {
            this.listener = listener as OnListener<out Any>?
        }

        @SingleClick
        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_ui_confirm -> {
                    val data = adapter.getSelectSet()
                    if (data.size >= adapter.getMinSelect()) {
                        autoDismiss()
                        listener?.onSelfSelected(getDialog(), data)
                    } else {
                        ToastUtils.show(String.format(getString(R.string.select_min_hint)!!, adapter.getMinSelect()))
                    }
                }
                R.id.tv_ui_cancel -> {
                    autoDismiss()
                    listener?.onCancel(getDialog())
                }
            }
        }

        /**
         * [View.OnLayoutChangeListener]
         */
        override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            recyclerView?.removeOnLayoutChangeListener(this)
            // 这里一定要加延迟，如果不加在 Android 9.0 上面会导致 setLayoutParams 无效
            post(this)
        }

        override fun run() {
            recyclerView?.let {
                val params = it.layoutParams ?: return
                val maxHeight = getScreenHeight() / 4 * 3
                if (it.height > maxHeight) {
                    if (params.height != maxHeight) {
                        params.height = maxHeight
                        it.layoutParams = params
                    }
                } else {
                    if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        it.layoutParams = params
                    }
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
                    ToastUtils.show(String.format(getString(R.string.select_max_hint)!!, maxSelect))
                }
            }
        }

        private inner class ViewHolder : AppViewHolder(R.layout.select_item) {

            private val textView: TextView? by lazy { findViewById(R.id.tv_select_text) }
            private val checkBox: CheckBox? by lazy { findViewById(R.id.tv_select_checkbox) }

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

    interface OnListener<T> {

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
}