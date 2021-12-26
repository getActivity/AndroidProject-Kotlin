package com.hjq.demo.ui.popup

import android.content.*
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BasePopupWindow
import com.hjq.base.action.AnimAction
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.other.ArrowDrawable
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/10/18
 *    desc   : 列表弹窗
 */
class ListPopup {

    class Builder(context: Context) : BasePopupWindow.Builder<Builder>(context),
        BaseAdapter.OnItemClickListener {

        private var listener: OnListener<Any>? = null
        private var autoDismiss = true
        private val adapter: MenuAdapter

        init {
            val recyclerView = RecyclerView(context)
            recyclerView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setContentView(recyclerView)
            adapter = MenuAdapter(getContext())
            adapter.setOnItemClickListener(this)
            recyclerView.adapter = adapter
            ArrowDrawable.Builder(context)
                .setArrowOrientation(Gravity.TOP)
                .setArrowGravity(Gravity.CENTER)
                .setShadowSize(context.resources.getDimension(R.dimen.dp_10).toInt())
                .setBackgroundColor(getColor(R.color.white))
                .apply(recyclerView)
        }

        override fun setGravity(gravity: Int): Builder = apply {
            when (gravity) {
                // 如果这个是在中间显示的
                Gravity.CENTER, Gravity.CENTER_VERTICAL -> {
                    // 重新设置动画
                    setAnimStyle(AnimAction.ANIM_SCALE)
                }
            }
            super.setGravity(gravity)
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

        fun setAutoDismiss(dismiss: Boolean): Builder = apply {
            autoDismiss = dismiss
        }

        @Suppress("UNCHECKED_CAST")
        fun setListener(listener: OnListener<out Any>?): Builder = apply {
            this.listener = listener as OnListener<Any>?
        }

        /**
         * [BaseAdapter.OnItemClickListener]
         */
        override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
            if (autoDismiss) {
                dismiss()
            }
            listener?.onSelected(getPopupWindow(), position, adapter.getItem(position))
        }
    }

    private class MenuAdapter(context: Context) : AppAdapter<Any>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder()
        }

        private inner class ViewHolder : AppAdapter<Any>.AppViewHolder(TextView(getContext())) {

            private val textView: TextView by lazy { getItemView() as TextView }

            init {
                textView.setTextColor(getColor(R.color.black50))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().resources.getDimension(R.dimen.sp_16))
            }

            override fun onBindView(position: Int) {
                textView.text = getItem(position).toString()
                textView.setPaddingRelative(
                    getContext().resources.getDimension(R.dimen.dp_12).toInt(),
                    if (position == 0) getContext().resources.getDimension(R.dimen.dp_12).toInt() else 0,
                    getContext().resources.getDimension(R.dimen.dp_12).toInt(),
                    getContext().resources.getDimension(R.dimen.dp_10).toInt())
            }
        }
    }

    interface OnListener<T> {

        /**
         * 选择条目时回调
         */
        fun onSelected(popupWindow: BasePopupWindow?, position: Int, data: T)
    }
}