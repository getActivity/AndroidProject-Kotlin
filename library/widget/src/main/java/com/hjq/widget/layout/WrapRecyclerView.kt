package com.hjq.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/21
 *    desc   : 支持添加头部和底部的 RecyclerView
 */
@Suppress("NotifyDataSetChanged")
class WrapRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RecyclerView(context, attrs, defStyleAttr) {

    /** 原有的适配器 */
    private var realAdapter: Adapter<out ViewHolder>? = null

    /** 支持添加头部和底部的适配器 */
    private var wrapAdapter: WrapRecyclerAdapter = WrapRecyclerAdapter()

    override fun setAdapter(adapter: Adapter<out ViewHolder>?) {
        realAdapter = adapter
        // 偷梁换柱
        wrapAdapter.setRealAdapter(realAdapter)
        // 禁用条目动画
        itemAnimator = null
        super.setAdapter(wrapAdapter)
    }

    override fun getAdapter(): Adapter<*>? {
        return realAdapter
    }

    /**
     * 添加头部View
     */
    fun addHeaderView(view: View?) {
        wrapAdapter.addHeaderView(view)
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : View?> addHeaderView(@LayoutRes id: Int): V {
        val headerView: View = LayoutInflater.from(context).inflate(id, this, false)
        addHeaderView(headerView)
        return headerView as V
    }

    /**
     * 移除头部View
     */
    fun removeHeaderView(view: View?) {
        wrapAdapter.removeHeaderView(view)
    }

    /**
     * 添加底部View
     */
    fun addFooterView(view: View?) {
        wrapAdapter.addFooterView(view)
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : View?> addFooterView(@LayoutRes id: Int): V? {
        val footerView: View = LayoutInflater.from(context).inflate(id, this, false)
        addFooterView(footerView)
        return footerView as V
    }

    /**
     * 移除底部View
     */
    fun removeFooterView(view: View?) {
        wrapAdapter.removeFooterView(view)
    }

    /**
     * 获取头部View总数
     */
    fun getHeaderViewsCount(): Int {
        return wrapAdapter.getHeaderViewsCount()
    }

    /**
     * 获取底部View总数
     */
    fun getFooterViewsCount(): Int {
        return wrapAdapter.getFooterViewsCount()
    }

    /**
     * 获取头部View集合
     */
    fun getHeaderViews(): MutableList<View?> {
        return wrapAdapter.getHeaderViews()
    }

    /**
     * 获取底部View集合
     */
    fun getFooterViews(): MutableList<View?> {
        return wrapAdapter.getFooterViews()
    }

    /**
     * 刷新头部和底部布局所有的 View 的状态
     */
    fun refreshHeaderFooterViews() {
        wrapAdapter.notifyDataSetChanged()
    }

    /**
     * 设置在 GridLayoutManager 模式下头部和尾部都是独占一行的效果
     */
    fun adjustSpanSize() {
        val layoutManager: LayoutManager? = layoutManager
        if (layoutManager !is GridLayoutManager) {
            return
        }

        layoutManager.spanSizeLookup = object : SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {
                return if (((position < wrapAdapter.getHeaderViewsCount()
                            || position >= wrapAdapter.getHeaderViewsCount() + (realAdapter?.itemCount ?: 0)))
                ) layoutManager.spanCount else 1
            }
        }
    }

    /**
     * 采用装饰设计模式，将原有的适配器包装起来
     */
    private class WrapRecyclerAdapter : Adapter<ViewHolder?>() {

        companion object {

            /** 头部条目类型 */
            private const val HEADER_VIEW_TYPE: Int = Int.MIN_VALUE shr 1

            /** 底部条目类型 */
            private const val FOOTER_VIEW_TYPE: Int = Int.MAX_VALUE shr 1
        }

        /** 原有的适配器 */
        private var realAdapter: Adapter<ViewHolder>? = null

        /** 头部View集合 */
        private val headerViews: MutableList<View?> = mutableListOf()

        /** 底部View集合 */
        private val footerViews: MutableList<View?> = mutableListOf()

        /** 当前调用的位置 */
        private var currentPosition: Int = 0

        /** RecyclerView对象 */
        private var recyclerView: RecyclerView? = null

        /** 数据观察者对象 */
        private var observer: WrapAdapterDataObserver? = null

        @Suppress("UNCHECKED_CAST")
        fun setRealAdapter(adapter: Adapter<out ViewHolder>?) {
            if (realAdapter === adapter) {
                return
            }
            // 为原有的 RecyclerAdapter 移除数据监听对象
            observer?.let { realAdapter?.unregisterAdapterDataObserver(it) }
            realAdapter = adapter as Adapter<ViewHolder>?
            if (realAdapter == null) {
                return
            }
            if (observer == null) {
                observer = WrapAdapterDataObserver(this)
            }
            // 为原有的 RecyclerAdapter 添加数据监听对象
            observer?.let { realAdapter?.registerAdapterDataObserver(it) }
            // 适配器不是第一次被绑定到 RecyclerView 上需要发送通知，因为第一次绑定会自动通知
            if (recyclerView != null) {
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            val itemCount = realAdapter?.itemCount ?: 0
            return getHeaderViewsCount() + itemCount + getFooterViewsCount()
        }

        override fun getItemViewType(position: Int): Int {
            currentPosition = position
            // 获取头部布局的总数
            val headerCount: Int = getHeaderViewsCount()
            // 获取原有适配器的总数
            val adapterCount: Int = realAdapter?.itemCount ?: 0
            // 获取在原有适配器上的位置
            val adjPosition: Int = position - headerCount
            if (position < headerCount) {
                return HEADER_VIEW_TYPE
            } else if (adjPosition < adapterCount) {
                return requireNotNull(realAdapter).getItemViewType(adjPosition)
            }
            return FOOTER_VIEW_TYPE
        }

        fun getPosition(): Int {
            return currentPosition
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                HEADER_VIEW_TYPE -> newWrapViewHolder(requireNotNull(headerViews[getPosition()]))
                FOOTER_VIEW_TYPE -> newWrapViewHolder(requireNotNull(footerViews[getPosition() - getHeaderViewsCount() - (realAdapter?.itemCount ?: 0)]))
                else -> {
                    val adapter = requireNotNull(realAdapter)
                    val itemViewType: Int = adapter.getItemViewType(getPosition() - getHeaderViewsCount())
                    if (itemViewType == HEADER_VIEW_TYPE || itemViewType == FOOTER_VIEW_TYPE) {
                        throw IllegalStateException("Please do not use this type as itemType")
                    }
                    return adapter.onCreateViewHolder(parent, itemViewType)
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (realAdapter == null) {
                return
            }
            when (getItemViewType(position)) {
                HEADER_VIEW_TYPE, FOOTER_VIEW_TYPE -> {
                    // default implementation ignored
                }
                else -> requireNotNull(realAdapter).onBindViewHolder(holder, getPosition() - getHeaderViewsCount())
            }
        }

        private fun newWrapViewHolder(view: View): WrapViewHolder {
            val parent: ViewParent? = view.parent
            if (parent is ViewGroup) {
                // IllegalStateException: ViewHolder views must not be attached when created.
                // Ensure that you are not passing 'true' to the attachToRoot parameter of LayoutInflater.inflate(..., boolean attachToRoot)
                parent.removeView(view)
            }
            return WrapViewHolder(view)
        }

        override fun getItemId(position: Int): Long {
            if ((realAdapter != null) && (position > getHeaderViewsCount() - 1) && (position < getHeaderViewsCount() + (realAdapter?.itemCount ?: 0))) {
                return requireNotNull(realAdapter).getItemId(position - getHeaderViewsCount())
            }
            return super.getItemId(position)
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            this.recyclerView = recyclerView
            realAdapter?.onAttachedToRecyclerView(recyclerView)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            this.recyclerView = null
            realAdapter?.onDetachedFromRecyclerView(recyclerView)
        }

        override fun onViewRecycled(holder: ViewHolder) {
            if (holder is WrapViewHolder) {
                // 防止这个 ViewHolder 被 RecyclerView 拿去复用
                holder.setIsRecyclable(false)
                return
            }
            realAdapter?.onViewRecycled(holder)
        }

        override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
            realAdapter.let {
                if (it == null || holder is WrapViewHolder) {
                    return super.onFailedToRecycleView(holder)
                }
                return it.onFailedToRecycleView(holder)
            }
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            realAdapter.let {
                if (it == null || holder is WrapViewHolder) {
                    return
                }
                it.onViewAttachedToWindow(holder)
            }
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            realAdapter.let {
                if (it == null || holder is WrapViewHolder) {
                    return
                }
                it.onViewDetachedFromWindow(holder)
            }
        }

        /**
         * 添加头部View
         */
        fun addHeaderView(view: View?) {
            if (view == null) {
                return
            }
            // 不能添加同一个 View 对象，否则会导致 RecyclerView 复用异常
            if (!headerViews.contains(view) && !footerViews.contains(view)) {
                headerViews.add(view)
                notifyDataSetChanged()
            }
        }

        /**
         * 移除头部View
         */
        fun removeHeaderView(view: View?) {
            if (view == null) {
                return
            }
            if (headerViews.remove(view)) {
                notifyDataSetChanged()
            }
        }

        /**
         * 添加底部View
         */
        fun addFooterView(view: View?) {
            if (view == null) {
                return
            }
            // 不能添加同一个 View 对象，否则会导致 RecyclerView 复用异常
            if (!footerViews.contains(view) && !headerViews.contains(view)) {
                footerViews.add(view)
                notifyDataSetChanged()
            }
        }

        /**
         * 移除底部View
         */
        fun removeFooterView(view: View?) {
            if (view == null) {
                return
            }
            if (footerViews.remove(view)) {
                notifyDataSetChanged()
            }
        }

        /**
         * 获取头部View总数
         */
        fun getHeaderViewsCount(): Int {
            return headerViews.size
        }

        /**
         * 获取底部View总数
         */
        fun getFooterViewsCount(): Int {
            return footerViews.size
        }

        /**
         * 获取头部View集合
         */
        fun getHeaderViews(): MutableList<View?> {
            return headerViews
        }

        /**
         * 获取底部View集合
         */
        fun getFooterViews(): MutableList<View?> {
            return footerViews
        }
    }

    /**
     * 头部和底部通用的ViewHolder对象
     */
    private class WrapViewHolder(itemView: View) : ViewHolder(itemView)

    /**
     * 数据改变监听器
     */
    private class WrapAdapterDataObserver(private val wrapAdapter: WrapRecyclerAdapter) : AdapterDataObserver() {

        override fun onChanged() {
            wrapAdapter.notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            wrapAdapter.notifyItemRangeChanged(wrapAdapter.getHeaderViewsCount() + positionStart, itemCount, payload)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            wrapAdapter.notifyItemRangeChanged(wrapAdapter.getHeaderViewsCount() + positionStart, itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            wrapAdapter.notifyItemRangeInserted(wrapAdapter.getHeaderViewsCount() + positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            wrapAdapter.notifyItemRangeRemoved(wrapAdapter.getHeaderViewsCount() + positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            wrapAdapter.notifyItemMoved(wrapAdapter.getHeaderViewsCount() + fromPosition, wrapAdapter.getHeaderViewsCount() + toPosition)
        }
    }
}