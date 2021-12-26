package com.hjq.demo.manager

import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.abs
import kotlin.math.min

/**
 *    author : 钉某人 & Android 轮子哥
 *    github : https://github.com/DingMouRen/LayoutManagerGroup
 *    time   : 2019/09/11
 *    desc   : 选择器布局管理器
 */
class PickerLayoutManager private constructor(
    context: Context, orientation: Int, reverseLayout: Boolean, maxItem: Int, scale: Float, alpha: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    private val linearSnapHelper: LinearSnapHelper = LinearSnapHelper()
    private val maxItem: Int
    private val scale: Float
    private val alpha: Boolean
    private var recyclerView: RecyclerView? = null
    private var listener: OnPickerListener? = null

    init {
        this.maxItem = maxItem
        this.alpha = alpha
        this.scale = scale
    }

    override fun onAttachedToWindow(recyclerView: RecyclerView) {
        super.onAttachedToWindow(recyclerView)
        this.recyclerView = recyclerView
        // 设置子控件的边界可以超过父布局的范围
        this.recyclerView!!.clipToPadding = false
        // 添加 LinearSnapHelper
        linearSnapHelper.attachToRecyclerView(this.recyclerView)
    }

    override fun onDetachedFromWindow(recyclerView: RecyclerView?, recycler: Recycler?) {
        super.onDetachedFromWindow(recyclerView, recycler)
        this.recyclerView = null
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return maxItem == 0
    }

    override fun onMeasure(recycler: Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        var width: Int = chooseSize(widthSpec, paddingLeft + paddingRight, ViewCompat.getMinimumWidth(recyclerView!!))
        var height: Int = chooseSize(heightSpec, paddingTop + paddingBottom, ViewCompat.getMinimumHeight(recyclerView!!))
        if (state.itemCount != 0 && maxItem != 0) {
            val itemView: View = recycler.getViewForPosition(0)
            measureChildWithMargins(itemView, widthSpec, heightSpec)
            if (orientation == HORIZONTAL) {
                val measuredWidth: Int = itemView.measuredWidth
                val paddingHorizontal: Int = (maxItem - 1) / 2 * measuredWidth
                recyclerView!!.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
                width = measuredWidth * maxItem
            } else if (orientation == VERTICAL) {
                val measuredHeight: Int = itemView.measuredHeight
                val paddingVertical: Int = (maxItem - 1) / 2 * measuredHeight
                recyclerView!!.setPadding(0, paddingVertical, 0, paddingVertical)
                height = measuredHeight * maxItem
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        // 当 RecyclerView 停止滚动时
        if (state != RecyclerView.SCROLL_STATE_IDLE) {
            return
        }
        recyclerView?.let {
            listener?.onPicked(it, getPickedPosition())
        }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        if (itemCount < 0 || state.isPreLayout) {
            return
        }
        if (orientation == HORIZONTAL) {
            scaleHorizontalChildView()
        } else if (orientation == VERTICAL) {
            scaleVerticalChildView()
        }
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler?, state: RecyclerView.State?): Int {
        scaleHorizontalChildView()
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler?, state: RecyclerView.State?): Int {
        scaleVerticalChildView()
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    /**
     * 横向情况下的缩放
     */
    private fun scaleHorizontalChildView() {
        val mid: Float = width / 2.0f
        for (i in 0 until childCount) {
            val childView: View = getChildAt(i) ?: continue
            val childMid: Float =
                (getDecoratedLeft(childView) + getDecoratedRight(childView)) / 2.0f
            val scale: Float = 1.0f + (-1 * (1 - scale)) * min(mid, abs(mid - childMid)) / mid
            childView.scaleX = scale
            childView.scaleY = scale
            if (alpha) {
                childView.alpha = scale
            }
        }
    }

    /**
     * 竖向方向上的缩放
     */
    private fun scaleVerticalChildView() {
        val mid: Float = height / 2.0f
        for (i in 0 until childCount) {
            val childView: View = getChildAt(i) ?: continue
            val childMid: Float = (getDecoratedTop(childView) + getDecoratedBottom(childView)) / 2.0f
            val scale: Float = 1.0f + (-1 * (1 - scale)) * (min(mid, abs(mid - childMid))) / mid
            childView.scaleX = scale
            childView.scaleY = scale
            if (alpha) {
                childView.alpha = scale
            }
        }
    }

    /**
     * 获取选中的位置
     */
    fun getPickedPosition(): Int {
        val itemView: View = linearSnapHelper.findSnapView(this) ?: return 0
        return getPosition(itemView)
    }

    /**
     * 设置监听器
     */
    fun setOnPickerListener(listener: OnPickerListener?) {
        this.listener = listener
    }

    interface OnPickerListener {

        /**
         * 滚动停止时触发的监听
         *
         * @param recyclerView              RecyclerView 对象
         * @param position                  当前滚动的位置
         */
        fun onPicked(recyclerView: RecyclerView, position: Int)
    }

    class Builder constructor(private val context: Context) {

        private var orientation: Int = VERTICAL
        private var reverseLayout: Boolean = false
        private var listener: OnPickerListener? = null
        private var maxItem: Int = 3
        private var scale: Float = 0.6f
        private var alpha: Boolean = true

        /**
         * 设置布局摆放器方向
         */
        fun setOrientation(@RecyclerView.Orientation orientation: Int): Builder = apply {
            this.orientation = orientation
        }

        /**
         * 设置是否反向显示
         */
        fun setReverseLayout(reverseLayout: Boolean): Builder = apply {
            this.reverseLayout = reverseLayout
        }

        /**
         * 设置最大显示条目数
         */
        fun setMaxItem(maxItem: Int): Builder = apply {
            this.maxItem = maxItem
        }

        /**
         * 设置缩放比例
         */
        fun setScale(scale: Float): Builder = apply {
            this.scale = scale
        }

        /**
         * 设置透明开关
         */
        fun setAlpha(alpha: Boolean): Builder = apply {
            this.alpha = alpha
        }

        fun setOnPickerListener(listener: OnPickerListener?): Builder = apply {
            this.listener = listener
        }

        /**
         * 构建布局管理器
         */
        fun build(): PickerLayoutManager {
            val layoutManager = PickerLayoutManager(context, orientation, reverseLayout, maxItem, scale, alpha)
            layoutManager.setOnPickerListener(listener)
            return layoutManager
        }

        /**
         * 应用到 RecyclerView
         */
        fun into(recyclerView: RecyclerView) {
            recyclerView.layoutManager = build()
        }
    }
}