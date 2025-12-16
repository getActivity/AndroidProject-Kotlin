package com.hjq.demo.ui.adapter.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/02/28
 *    desc   : 导航栏适配器
 */
class NavigationAdapter(context: Context) :
    AppAdapter<NavigationAdapter.NavigationItem>(context), BaseAdapter.OnItemClickListener {

    /** 当前选中条目位置 */
    private var selectedPosition: Int = 0

    /** 导航栏点击监听 */
    private var listener: OnNavigationListener? = null

    init {
        setOnItemClickListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return ViewHolder()
    }

    override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return GridLayoutManager(context, getCount(), RecyclerView.VERTICAL, false)
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    /**
     * 设置导航栏监听
     */
    fun setOnNavigationListener(listener: OnNavigationListener?) {
        this.listener = listener
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     */
    override fun onItemClick(recyclerView: RecyclerView, itemView: View, position: Int) {
        if (selectedPosition == position) {
            return
        }

        if (listener?.onNavigationItemSelected(position) != false) {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder : AppViewHolder(R.layout.home_navigation_item) {

        private val iconView: ImageView? by lazyFindViewById(R.id.iv_home_navigation_icon)
        private val titleView: TextView? by lazyFindViewById(R.id.tv_home_navigation_title)

        override fun onBindView(position: Int) {
            getItem(position).apply {
                iconView?.setImageDrawable(getDrawable())
                titleView?.text = getText()
                iconView?.isSelected = (selectedPosition == position)
                titleView?.isSelected = (selectedPosition == position)
            }
        }
    }

    class NavigationItem(private val text: String?, private val drawable: Drawable?) {

        fun getText(): String? {
            return text
        }

        fun getDrawable(): Drawable? {
            return drawable
        }
    }

    interface OnNavigationListener {
        fun onNavigationItemSelected(position: Int): Boolean
    }
}