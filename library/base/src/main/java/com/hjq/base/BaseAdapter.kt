package com.hjq.base

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.core.action.ResourcesAction


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

    override fun onBindViewHolder(holder: VH, position: Int) {
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

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        holder.onAttached()
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetached()
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    /**
     * 条目 ViewHolder，需要子类 ViewHolder 继承
     */
    abstract inner class BaseViewHolder(itemView: View) :
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
         * ViewHolder 绑定到窗口回调
         */
        open fun onAttached() {
            // default implementation ignored
        }

        /**
         * ViewHolder 从窗口解绑回调
         */
        open fun onDetached() {
            // default implementation ignored
        }

        /**
         * ViewHolder 回收回调
         */
        open fun onRecycled() {
            // default implementation ignored
        }

        /**
         * 获取 ViewHolder 位置
         */
        protected open fun getViewHolderPosition(): Int {
            return layoutPosition
        }

        /**
         * [View.OnClickListener]
         */
        override fun onClick(view: View) {
            val position: Int = getViewHolderPosition()
            if (position !in 0..<itemCount) {
                return
            }
            if (view === getItemView()) {
                itemClickListener?.onItemClick(requireNotNull(recyclerView), view, position)
                return
            }
            childClickListeners.get(view.id)?.onChildClick(requireNotNull(recyclerView), view, position)
        }

        /**
         * [View.OnLongClickListener]
         */
        override fun onLongClick(view: View): Boolean {
            val position: Int = getViewHolderPosition()
            if (position !in 0..<itemCount) {
                return false
            }
            if (view === getItemView()) {
                return itemLongClickListener?.onItemLongClick(requireNotNull(recyclerView), view, position) ?: false
            }
            val listener: OnChildLongClickListener? = childLongClickListeners.get(view.id)
            if (listener != null) {
                return listener.onChildLongClick(requireNotNull(recyclerView), view, position)
            }
            return false
        }

        open fun getItemView(): View {
            return itemView
        }

        open fun <V : View?> findViewById(@IdRes id: Int): V? {
            return itemView.findViewById(id)
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
    protected open fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }

    /**
     * 设置 RecyclerView 条目点击监听
     */
    open fun setOnItemClickListener(listener: OnItemClickListener?) {
        checkListenerEffective()
        itemClickListener = listener
    }

    /**
     * 设置 RecyclerView 条目子 View 点击监听
     */
    open fun setOnChildClickListener(@IdRes id: Int, listener: OnChildClickListener?) {
        checkListenerEffective()
        childClickListeners.put(id, listener)
    }

    /**
     * 设置 RecyclerView 条目长按监听
     */
    open fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        checkListenerEffective()
        itemLongClickListener = listener
    }

    /**
     * 设置 RecyclerView 条目子 View 长按监听
     */
    open fun setOnChildLongClickListener(@IdRes id: Int, listener: OnChildLongClickListener?) {
        checkListenerEffective()
        childLongClickListeners.put(id, listener)
    }

    /**
     * 检查监听器是否有效
     */
    private fun checkListenerEffective() {
        if (recyclerView == null) {
            return
        }
        // 必须在 RecyclerView.setAdapter() 之前设置监听
        throw IllegalStateException("You must set the listener before RecyclerView.setAdapter()")
    }

    /**
     * RecyclerView 条目点击监听类
     */
    fun interface OnItemClickListener {

        /**
         * 当 RecyclerView 某个条目被点击时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param itemView          被点击的条目对象
         * @param position          被点击的条目位置
         */
        fun onItemClick(recyclerView: RecyclerView, itemView: View, position: Int)
    }

    /**
     * RecyclerView 条目长按监听类
     */
    fun interface OnItemLongClickListener {

        /**
         * 当 RecyclerView 某个条目被长按时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param itemView          被点击的条目对象
         * @param position          被点击的条目位置
         * @return                  是否拦截事件
         */
        fun onItemLongClick(recyclerView: RecyclerView, itemView: View, position: Int): Boolean
    }

    /**
     * RecyclerView 条目子 View 点击监听类
     */
    fun interface OnChildClickListener {

        /**
         * 当 RecyclerView 某个条目 子 View 被点击时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param childView         被点击的条目子 View
         * @param position          被点击的条目位置
         */
        fun onChildClick(recyclerView: RecyclerView, childView: View, position: Int)
    }

    /**
     * RecyclerView 条目子 View 长按监听类
     */
    fun interface OnChildLongClickListener {

        /**
         * 当 RecyclerView 某个条目子 View 被长按时回调
         *
         * @param recyclerView      RecyclerView 对象
         * @param childView         被点击的条目子 View
         * @param position          被点击的条目位置
         */
        fun onChildLongClick(recyclerView: RecyclerView, childView: View, position: Int): Boolean
    }
}