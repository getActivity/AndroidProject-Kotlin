package com.hjq.base

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : FragmentPagerAdapter 封装
 */
@Suppress("deprecation")
open class FragmentPagerAdapter<F : Fragment> constructor(manager: FragmentManager) :
    FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /** Fragment 集合 */
    private val fragmentSet: MutableList<F> = ArrayList()

    /** Fragment 标题 */
    private val fragmentTitle: MutableList<CharSequence?> = ArrayList()

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
        return getItem(position).hashCode().toLong()
    }

    override fun getCount(): Int {
        return fragmentSet.size
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
    @JvmOverloads
    open fun addFragment(fragment: F, title: CharSequence? = null) {
        fragmentSet.add(fragment)
        fragmentTitle.add(title ?: "")
        if (viewPager == null) {
            return
        }
        notifyDataSetChanged()
        viewPager?.offscreenPageLimit = if (lazyMode) count else 1
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