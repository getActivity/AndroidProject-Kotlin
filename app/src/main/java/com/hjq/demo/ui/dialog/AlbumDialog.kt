package com.hjq.demo.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.base.BottomSheetDialog
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.http.glide.GlideApp

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/07/27
 *    desc   : 相册专辑选取对话框
 */
class AlbumDialog {
    
    class Builder(context: Context) : BaseDialog.Builder<Builder>(context), BaseAdapter.OnItemClickListener {

        private var listener: OnListener? = null
        private val recyclerView: RecyclerView? by lazy { findViewById(R.id.rv_album_list) }
        private val adapter: AlbumAdapter

        init {
            setContentView(R.layout.album_dialog)
            adapter = AlbumAdapter(context)
            adapter.setOnItemClickListener(this)
            recyclerView?.adapter = adapter
        }

        fun setData(data: MutableList<AlbumInfo>): Builder = apply {
            adapter.setData(data)
            // 滚动到选中的位置
            for (i in data.indices) {
                if (data[i].isSelect()) {
                    recyclerView?.scrollToPosition(i)
                    break
                }
            }
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
            val data = adapter.getData()
            for (info in data) {
                if (info.isSelect()) {
                    info.setSelect(false)
                    break
                }
            }
            adapter.getItem(position).setSelect(true)
            adapter.notifyDataSetChanged()

            // 延迟消失
            postDelayed({
                listener?.onSelected(getDialog(), position, adapter.getItem(position))
                dismiss()
            }, 300)
        }

        override fun createDialog(context: Context, themeId: Int): BaseDialog {
            val dialog = BottomSheetDialog(context, themeId)
            dialog.getBottomSheetBehavior().peekHeight = getResources().displayMetrics.heightPixels / 2
            return dialog
        }
    }

    class AlbumAdapter constructor(context: Context) : AppAdapter<AlbumInfo>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder()
        }

        inner class ViewHolder : AppViewHolder(R.layout.album_item) {

            private val iconView: ImageView? by lazy { findViewById(R.id.iv_album_icon) }
            private val nameView: TextView? by lazy { findViewById(R.id.tv_album_name) }
            private val remarkView: TextView? by lazy { findViewById(R.id.tv_album_remark) }
            private val checkBox: CheckBox? by lazy { findViewById(R.id.rb_album_check) }

            override fun onBindView(position: Int) {
                getItem(position).apply {
                    iconView?.let {
                        GlideApp.with(getContext())
                            .asBitmap()
                            .load(getIcon())
                            .into(it)
                    }
                    nameView?.text = getName()
                    remarkView?.text = getRemark()
                    checkBox?.isChecked = isSelect()
                    checkBox?.visibility = if (isSelect()) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    /**
     * 专辑信息类
     */
    class AlbumInfo(

        /** 封面 */
        private val icon: String,
        /** 名称 */
        private var name: String,
        /** 备注 */
        private val remark: String,
        /** 选中 */
        private var select: Boolean) {

        fun setName(name: String) {
            this.name = name
        }

        fun setSelect(select: Boolean) {
            this.select = select
        }

        fun getIcon(): String {
            return icon
        }

        fun getName(): String {
            return name
        }

        fun getRemark(): String {
            return remark
        }

        fun isSelect(): Boolean {
            return select
        }
    }

    interface OnListener {

        /**
         * 选择条目时回调
         */
        fun onSelected(dialog: BaseDialog?, position: Int, bean: AlbumInfo)
    }
}