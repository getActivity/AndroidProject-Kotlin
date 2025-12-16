package com.hjq.base

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : FragmentPagerAdapter 封装
 */
@Suppress("deprecation")
open class BasePagerAdapter<F : Fragment>(manager: FragmentManager) :
    FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /** Fragment 集合 */
    private val fragmentSet: MutableList<F> = mutableListOf()

    /** Fragment 标题 */
    private val fragmentTitle: MutableList<CharSequence?> = mutableListOf()

    /** 当前显示的Fragment */
    private var showFragment: F? = null

    /** 当前 ViewPager */
    private var viewPager: ViewPager? = null

    /** 设置成懒加载模式 */
    private var lazyMode: Boolean = true

    constructor(activity: FragmentActivity) : this(activity.supportFragmentManager)

    constructor(fragment: Fragment) : this(fragment.childFragmentManager)

    override fun getItem(position: Int): F {
        return fragmentSet[position]
    }

    override fun getItemId(position: Int): Long {
        if (position >= count) {
            return super.getItemId(position)
        }
        return getItem(position).hashCode().toLong()
    }

    override fun getCount(): Int {
        return fragmentSet.size
    }

    override fun getItemPosition(`object`: Any): Int {
        if (`object` is Fragment && !fragmentSet.contains(`object`)) {
            return POSITION_NONE
        }
        return super.getItemPosition(`object`)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitle[position]
    }

    @Suppress("UNCHECKED_CAST")
    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (getShowFragment() !== `object`) {
            // 记录当前的Fragment对象
            showFragment = `object` as F
        }
    }

    /**
     * 添加 Fragment
     */
    open fun addFragment(fragment: F, title: CharSequence? = "", fragmentIndex: Int = fragmentSet.size) {
        // 避免集合角标越界
        if (fragmentIndex > fragmentSet.size) {
            return
        }
        fragmentSet.add(fragmentIndex, fragment)
        fragmentTitle.add(fragmentIndex, title)
        viewPager.let {
            if (it == null) {
                return
            }

            notifyDataSetChanged()
            if (lazyMode) {
                it.offscreenPageLimit = count
            } else {
                it.offscreenPageLimit = 1
            }
        }
    }

    open fun removeFragment(clazz: Class<out Fragment?>?) {
        val fragmentIndex = getFragmentIndex(clazz)
        if (fragmentIndex <= 0) {
            return
        }
        removeFragment(fragmentIndex)
    }

    open fun removeFragment(fragment: Fragment?) {
        val fragmentIndex: Int = fragmentSet.indexOf(fragment)
        if (fragmentIndex <= 0) {
            return
        }
        removeFragment(fragmentIndex)
    }

    open fun removeFragment(fragmentIndex: Int) {
        fragmentSet.removeAt(fragmentIndex)
        fragmentTitle.removeAt(fragmentIndex)

        viewPager.let {
            if (it == null) {
                return
            }

            val currentItem: Int = it.currentItem
            if (currentItem > 0 && fragmentIndex == currentItem && fragmentIndex == fragmentSet.size - 1) {
                // 则先切换到上一个 Fragment 再进行删除，避免出现角标异常
                it.setCurrentItem(currentItem - 1, false)
            }
            notifyDataSetChanged()
            if (lazyMode) {
                it.offscreenPageLimit = count
            } else {
                it.offscreenPageLimit = 1
            }
        }
    }

    /**
     * 获取当前的Fragment
     */
    open fun getShowFragment(): F? {
        return showFragment
    }

    /**
     * 获取某个 Fragment 的索引（没有就返回 -1）
     */
    open fun getFragmentIndex(clazz: Class<out Fragment?>?): Int {
        if (clazz == null) {
            return -1
        }
        for (i in fragmentSet.indices) {
            if ((clazz.name == fragmentSet[i].javaClass.name)) {
                return i
            }
        }
        return -1
    }

    override fun startUpdate(container: ViewGroup) {
        super.startUpdate(container)
        if (container is ViewPager) {
            // 记录绑定 ViewPager
            viewPager = container
            refreshLazyMode()
        }
    }

    /**
     * 设置懒加载模式
     */
    open fun setLazyMode(lazy: Boolean) {
        lazyMode = lazy
        refreshLazyMode()
    }

    /**
     * 刷新加载模式
     */
    private fun refreshLazyMode() {
        if (viewPager == null) {
            return
        }
        // 设置成懒加载模式（也就是不限制 Fragment 展示的数量）
        viewPager?.offscreenPageLimit = if (lazyMode) count else 1
    }
}