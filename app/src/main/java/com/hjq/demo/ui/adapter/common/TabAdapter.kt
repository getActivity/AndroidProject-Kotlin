package com.hjq.demo.ui.adapter.common

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.hjq.base.BaseAdapter
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.app.AppAdapter

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/02/28
 *    desc   : Tab 适配器
 */
class TabAdapter @JvmOverloads constructor(
    context: Context,
    /** Tab 样式 */
    private val tabMode: Int = TAB_MODE_DESIGN,
    /** Tab 宽度是否固定 */
    private val fixed: Boolean = true
) : AppAdapter<String?>(context), BaseAdapter.OnItemClickListener {

    companion object {
        const val TAB_MODE_DESIGN: Int = 1
        const val TAB_MODE_SLIDING: Int = 2
    }

    /** 当前选中条目位置 */
    private var selectedPosition: Int = 0

    /** 导航栏监听对象 */
    private var listener: OnTabListener? = null

    init {
        setOnItemClickListener(this)
        registerAdapterDataObserver(TabAdapterDataObserver())
    }

    override fun getItemViewType(position: Int): Int {
        return tabMode
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return when (viewType) {
            TAB_MODE_DESIGN -> DesignViewHolder()
            TAB_MODE_SLIDING -> SlidingViewHolder()
            else -> throw IllegalArgumentException("are you ok?")
        }
    }

    override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return if (fixed) {
            var count: Int = getCount()
            if (count < 1) {
                count = 1
            }
            GridLayoutManager(context, count, RecyclerView.VERTICAL, false)
        } else {
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        // 禁用 RecyclerView 条目动画
        recyclerView.itemAnimator = null
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    fun setSelectedPosition(position: Int) {
        if (selectedPosition == position) {
            return
        }

        val oldPosition: Int = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)

        // 如果当前 Tab 宽度是对等的模式，则不继续往下执行
        if (fixed) {
            return
        }

        // 注意：GridLayoutManager 是 LinearLayoutManager 的子类
        val linearLayoutManager = getRecyclerView()?.layoutManager as? LinearLayoutManager ?: return

        // 获取第一个和最后一个完全可见的条目位置
        val firstCompletelyVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        val lastCompletelyVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()

        // 如果要滚动的 Tab 是在完全可视区域范围内，则不往下执行
        if (position in firstCompletelyVisibleItemPosition..lastCompletelyVisibleItemPosition) {
            return
        }
        // 处理在 Tab 过多的情况下，选中某个 Tab 选项中没有自动滚动的问题
        linearLayoutManager.scrollToPosition(position)
    }

    /**
     * 设置导航栏监听
     */
    fun setOnTabListener(listener: OnTabListener?) {
        this.listener = listener
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     */
    override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
        if (selectedPosition == position) {
            return
        }

        val runnable = Runnable {
            val oldPosition: Int = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }

        listener.let {
            if (it == null) {
                runnable.run()
                return
            }

            if (it.onTabSelected(recyclerView, position)) {
                runnable.run()
            }
        }
    }

    inner class DesignViewHolder : AppViewHolder(R.layout.tab_item_design) {

        private val titleView: TextView? by lazyFindViewById(R.id.tv_tab_design_title)
        private val lineView: View? by lazyFindViewById(R.id.v_tab_design_line)

        init {
            if (fixed) {
                val itemView: View = getItemView()
                val layoutParams: ViewGroup.LayoutParams = itemView.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                itemView.layoutParams = layoutParams
            }
        }

        override fun onBindView(position: Int) {
            titleView?.text = getItem(position)
            titleView?.isSelected = (selectedPosition == position)
            lineView?.visibility = if (selectedPosition == position) View.VISIBLE else View.INVISIBLE
        }
    }

    inner class SlidingViewHolder : AppViewHolder(R.layout.tab_item_sliding), AnimatorUpdateListener {

        private val titleView: TextView? by lazyFindViewById(R.id.tv_tab_sliding_title)
        private val lineView: View? by lazyFindViewById(R.id.v_tab_sliding_line)

        private val defaultTextSize: Int by lazy { getResources().getDimension(R.dimen.sp_14).toInt() }
        private val selectedTextSize: Int by lazy { getResources().getDimension(R.dimen.sp_15).toInt() }

        init {
            titleView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize.toFloat())
            if (fixed) {
                val itemView: View = getItemView()
                val layoutParams: ViewGroup.LayoutParams = itemView.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                itemView.layoutParams = layoutParams
            }
        }

        override fun onBindView(position: Int) {
            lineView?.visibility = if (selectedPosition == position) View.VISIBLE else View.INVISIBLE
            titleView?.let {
                it.text = getItem(position)
                it.isSelected = (selectedPosition == position)
                val textSize: Int = it.textSize.toInt()
                if (selectedPosition == position) {
                    if (textSize != selectedTextSize) {
                        startAnimator(defaultTextSize, selectedTextSize)
                    }
                    return
                }
                if (textSize != defaultTextSize) {
                    startAnimator(selectedTextSize, defaultTextSize)
                }
            }
        }

        private fun startAnimator(start: Int, end: Int) {
            val valueAnimator: ValueAnimator = ValueAnimator.ofInt(start, end)
            valueAnimator.addUpdateListener(this)
            valueAnimator.duration = 100
            valueAnimator.start()
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            titleView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, (animation.animatedValue as Int).toFloat())
        }
    }

    /**
     * 数据改变监听器
     */
    private inner class TabAdapterDataObserver : AdapterDataObserver() {

        override fun onChanged() {
            refreshLayoutManager()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {}

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {}

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            refreshLayoutManager()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            refreshLayoutManager()
            if (getSelectedPosition() > positionStart - itemCount) {
                setSelectedPosition(positionStart - itemCount)
            }
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {}

        private fun refreshLayoutManager() {
            if (!fixed) {
                return
            }
            getRecyclerView()?.layoutManager = generateDefaultLayoutManager(getContext())
        }
    }

    /**
     * Tab 监听器
     */
    interface OnTabListener {

        /**
         * Tab 被选中了
         */
        fun onTabSelected(recyclerView: RecyclerView?, position: Int): Boolean
    }
}