package com.hjq.demo.app

import android.content.Context
import android.view.View
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import com.hjq.base.BaseAdapter
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/19
 *    desc   : RecyclerView 适配器业务基类
 */
abstract class AppAdapter<T> constructor(context: Context) :
    BaseAdapter<AppAdapter<T>.AppViewHolder>(context) {

    /** 列表数据 */
    private var dataSet: MutableList<T> = ArrayList()

    /** 当前列表的页码，默认为第一页，用于分页加载功能 */
    private var pageNumber = 1

    /** 是否是最后一页，默认为false，用于分页加载功能 */
    private var lastPage = false

    /** 标记对象 */
    private var tag: Any? = null

    override fun getItemCount(): Int {
        return getCount()
    }

    /**
     * 获取数据总数
     */
    open fun getCount(): Int {
        return dataSet.size
    }

    /**
     * 设置新的数据
     */
    open fun setData(data: MutableList<T>?) {
        if (data == null) {
            dataSet.clear()
        } else {
            dataSet = data
        }
        notifyDataSetChanged()
    }

    /**
     * 获取当前数据
     */
    open fun getData(): MutableList<T> {
        return dataSet
    }

    /**
     * 追加一些数据
     */
    open fun addData(data: MutableList<T>?) {
        if (data == null || data.isEmpty()) {
            return
        }
        dataSet.addAll(data)
        notifyItemRangeInserted(dataSet.size - data.size, data.size)
    }

    /**
     * 清空当前数据
     */
    open fun clearData() {
        dataSet.clear()
        notifyDataSetChanged()
    }

    /**
     * 是否包含了某个位置上的条目数据
     */
    open fun containsItem(@IntRange(from = 0) position: Int): Boolean {
        return containsItem(getItem(position))
    }

    /**
     * 是否包含某个条目数据
     */
    open fun containsItem(item: T?): Boolean {
        return if (item == null) {
            false
        } else dataSet.contains(item)
    }

    /**
     * 获取某个位置上的数据
     */
    open fun getItem(@IntRange(from = 0) position: Int): T {
        return dataSet[position]
    }

    /**
     * 更新某个位置上的数据
     */
    open fun setItem(@IntRange(from = 0) position: Int, item: T) {
        dataSet[position] = item
        notifyItemChanged(position)
    }

    /**
     * 添加单条数据
     */
    open fun addItem(item: T) {
        addItem(dataSet.size, item)
    }

    open fun addItem(@IntRange(from = 0) position: Int, item: T) {
        var finalPosition = position
        if (finalPosition < dataSet.size) {
            dataSet.add(finalPosition, item)
        } else {
            dataSet.add(item)
            finalPosition = dataSet.size - 1
        }
        notifyItemInserted(finalPosition)
    }

    /**
     * 删除单条数据
     */
    open fun removeItem(item: T) {
        val index = dataSet.indexOf(item)
        if (index != -1) {
            removeItem(index)
        }
    }

    open fun removeItem(@IntRange(from = 0) position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * 获取当前的页码
     */
    open fun getPageNumber(): Int {
        return pageNumber
    }

    /**
     * 设置当前的页码
     */
    open fun setPageNumber(@IntRange(from = 0) number: Int) {
        pageNumber = number
    }

    /**
     * 当前是否为最后一页
     */
    open fun isLastPage(): Boolean {
        return lastPage
    }

    /**
     * 设置是否为最后一页
     */
    open fun setLastPage(last: Boolean) {
        lastPage = last
    }

    /**
     * 获取标记
     */
    open fun getTag(): Any? {
        return tag
    }

    /**
     * 设置标记
     */
    open fun setTag(tag: Any) {
        this.tag = tag
    }

    abstract inner class AppViewHolder : BaseViewHolder {

        constructor(@LayoutRes id: Int) : super(id)

        constructor(itemView: View) : super(itemView)
    }

    inner class SimpleViewHolder : AppViewHolder {

        constructor(@LayoutRes id: Int) : super(id)

        constructor(itemView: View) : super(itemView)

        override fun onBindView(position: Int) {}
    }
}