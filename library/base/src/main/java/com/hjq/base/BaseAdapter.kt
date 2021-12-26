package com.hjq.base

import android.content.*
import android.util.SparseArray
import android.view.*
import android.view.View.OnLongClickListener
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.action.ResourcesAction

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : RecyclerView 适配器技术基类
 */
@Suppress("LeakingThis")
abstract class BaseAdapter<VH : BaseAdapter<VH>.BaseViewHolder> (private val context: Context) :
    RecyclerView.Adapter<VH>(), ResourcesAction {

    /** RecyclerView 对象 */
    private var recyclerView: RecyclerView? = null

    /** 条目点击监听器 */
    private var itemClickListener: OnItemClickListener? = null

    /** 条目长按监听器 */
    private var itemLongClickListener: OnItemLongClickListener? = null

    /** 条目子 View 点击监听器 */
    private val childClickListeners: SparseArray<OnChildClickListener?> by lazy { SparseArray() }

    /** 条目子 View 长按监听器 */
    private val childLongClickListeners: SparseArray<OnChildLongClickListener?> by lazy { SparseArray() }

    /** ViewHolder 位置偏移值 */
    private var positionOffset: Int = 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        // 根据 ViewHolder 绑定的位置和传入的位置进行对比
        // 一般情况下这两个位置值是相等的，但是有一种特殊的情况
        // 在外层添加头部 View 的情况下，这两个位置值是不对等的
        positionOffset = position - holder.adapterPosition
        holder.onBindView(position)
    }

    /**
     * 获取 RecyclerView 对象
     */
    open fun getRecyclerView(): RecyclerView? {
        return recyclerView
    }

    override fun getContext(): Context {
        return context
    }

    /**
     * 条目 ViewHolder，需要子类 ViewHolder 继承
     */
    abstract inner class BaseViewHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, OnLongClickListener {

        constructor(@LayoutRes id: Int) : this(
            LayoutInflater.from(getContext()).inflate(id, recyclerView, false)
        )

        init {
            // 设置条目的点击和长按事件
            if (itemClickListener != null) {
                itemView.setOnClickListener(this)
            }

            if (itemLongClickListener != null) {
                itemView.setOnLongClickListener(this)
            }

            // 设置条目子 View 点击事件
            for (i in 0 until childClickListeners.size()) {
                findViewById<View>(childClickListeners.keyAt(i))?.setOnClickListener(this)
            }

            // 设置条目子 View 长按事件
            for (i in 0 until childLongClickListeners.size()) {
                findViewById<View>(childLongClickListeners.keyAt(i))?.setOnLongClickListener(this)
            }
        }

        /**
         * 数据绑定回调
         */
        abstract fun onBindView(position: Int)

        /**
         * 获取 ViewHolder 位置
         */
        protected open fun getViewHolderPosition(): Int {
            // 这里解释一下为什么用 getLayoutPosition 而不用 getAdapterPosition
            // 如果是使用 getAdapterPosition 会导致一个问题，那就是快速点击删除条目的时候会出现 -1 的情况，因为这个 ViewHolder 已经解绑了
            // 而使用 getLayoutPosition 则不会出现位置为 -1 的情况，因为解绑之后在布局中不会立马消失，所以不用担心在动画执行中获取位置有异常的情况
            return layoutPosition + positionOffset
        }

        /**
         * [View.OnClickListener]
         */
        override fun onClick(view: View) {
            val position: Int = getViewHolderPosition()
            if (position < 0 || position >= itemCount) {
                return
            }
            if (view === getItemView()) {
                itemClickListener?.onItemClick(recyclerView, view, position)
                return
            }
            childClickListeners.get(view.id)?.onChildClick(recyclerView, view, position)
        }

        /**
         * [View.OnLongClickListener]
         */
        override fun onLongClick(view: View): Boolean {
            val position: Int = getViewHolderPosition()
            if (position < 0 || position >= itemCount) {
                return false
            }
            if (view === getItemView()) {
                if (itemLongClickListener != null) {
                    return itemLongClickListener!!.onItemLongClick(recyclerView, view, position)
                }
                return false
            }
            val listener: OnChildLongClickListener? = childLongClickListeners.get(view.id)
            if (listener != null) {
                return listener.onChildLongClick(recyclerView, view, position)
            }
            return false
        }

        open fun getItemView(): View {
            return itemView
        }

        open fun <V : View?> findViewById(@IdRes id: Int): V? {
            return getItemView().findViewById(id)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        // 判断当前的布局管理器是否为空，如果为空则设置默认的布局管理器
        if (this.recyclerView?.layoutManager == null) {
            this.recyclerView?.layoutManager = generateDefaultLayoutManager(context)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    /**
     * 生成默认的布局摆放器
     */
    protected open fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager? {
        return LinearLayoutManager(context)
    }

    /**
     * 设置 RecyclerView 条目点击监听
     */
    open fun setOnItemClickListener(listener: OnItemClickListener?) {
        checkRecyclerViewState()
        itemClickListener = listener
    }

    /**
     * 设置 RecyclerView 条目子 View 点击监听
     */
    open fun setOnChildClickListener(@IdRes id: Int, listener: OnChildClickListener?) {
        checkRecyclerViewState()
        childClickListeners.put(id, listener)
    }

    /**
     * 设置 RecyclerView 条目长按监听
     */
    open fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        checkRecyclerViewState()
        itemLongClickListener = listener
    }

    /**
     * 设置 RecyclerView 条目子 View 长按监听
     */
    open fun setOnChildLongClickListener(@IdRes id: Int, listener: OnChildLongClickListener?) {
        checkRecyclerViewState()
        childLongClickListeners.put(id, listener)
    }

    /**
     * 检查 RecyclerView 状态
     */
    private fun checkRecyclerViewState() {
        if (recyclerView != null) {
            // 必须在 RecyclerView.setAdapter() 之前设置监听
            throw IllegalStateException("are you ok?")
        }
    }

    /**
     * RecyclerView 条目点击监听类
     */
    interface OnItemClickListener {

        /**
         * 当 RecyclerView 某个条目被点击时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param itemView          被点击的条目对象
         * @param position          被点击的条目位置
         */
        fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int)
    }

    /**
     * RecyclerView 条目长按监听类
     */
    interface OnItemLongClickListener {

        /**
         * 当 RecyclerView 某个条目被长按时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param itemView          被点击的条目对象
         * @param position          被点击的条目位置
         * @return                  是否拦截事件
         */
        fun onItemLongClick(recyclerView: RecyclerView?, itemView: View?, position: Int): Boolean
    }

    /**
     * RecyclerView 条目子 View 点击监听类
     */
    interface OnChildClickListener {

        /**
         * 当 RecyclerView 某个条目 子 View 被点击时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param childView         被点击的条目子 View
         * @param position          被点击的条目位置
         */
        fun onChildClick(recyclerView: RecyclerView?, childView: View?, position: Int)
    }

    /**
     * RecyclerView 条目子 View 长按监听类
     */
    interface OnChildLongClickListener {

        /**
         * 当 RecyclerView 某个条目子 View 被长按时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param childView         被点击的条目子 View
         * @param position          被点击的条目位置
         */
        fun onChildLongClick(recyclerView: RecyclerView?, childView: View?, position: Int): Boolean
    }
}