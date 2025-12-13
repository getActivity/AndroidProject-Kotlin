package com.hjq.demo.ui.fragment.home

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.BasePagerAdapter
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.app.AppFragment
import com.hjq.demo.app.TitleBarFragment
import com.hjq.demo.ui.activity.HomeActivity
import com.hjq.demo.ui.adapter.common.TabAdapter
import com.hjq.demo.ui.adapter.common.TabAdapter.OnTabListener
import com.hjq.demo.ui.fragment.StatusFragment
import com.hjq.demo.ui.fragment.common.BrowserFragment
import com.hjq.demo.widget.XCollapsingToolbarLayout
import com.hjq.demo.widget.XCollapsingToolbarLayout.OnScrimsListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 首页 Fragment
 */
class HomeMainFragment : TitleBarFragment<HomeActivity>(), OnTabListener,
    OnPageChangeListener, OnScrimsListener {

    companion object {

        fun newInstance(): HomeMainFragment {
            return HomeMainFragment()
        }
    }

    private val collapsingToolbarLayout: XCollapsingToolbarLayout? by lazyFindViewById(R.id.ctl_home_main_bar)
    private val addressView: TextView? by lazyFindViewById(R.id.tv_home_main_address)
    private val hintView: TextView? by lazyFindViewById(R.id.tv_home_main_hint)
    private val searchView: AppCompatImageView? by lazyFindViewById(R.id.iv_home_main_search)
    private val tabView: RecyclerView? by lazyFindViewById(R.id.rv_home_main_tab)
    private val viewPager: ViewPager? by lazyFindViewById(R.id.vp_home_main_pager)

    private var tabAdapter: TabAdapter? = null
    private var pagerAdapter: BasePagerAdapter<AppFragment<*>>? = null

    override fun getLayoutId(): Int {
        return R.layout.home_main_fragment
    }

    override fun initView() {
        pagerAdapter = BasePagerAdapter<AppFragment<*>>(this).apply {
            addFragment(StatusFragment.newInstance(), "列表演示")
            addFragment(BrowserFragment.newInstance("https://github.com/getActivity"), "网页演示")
        }
        viewPager?.adapter = pagerAdapter
        viewPager?.addOnPageChangeListener(this)
        tabAdapter = TabAdapter(getAttachActivity()!!)
        tabView?.adapter = tabAdapter

        ImmersionBar.setTitleBarMarginTop(getAttachActivity(), findViewById(R.id.tb_home_main_title))

        // 设置渐变监听
        collapsingToolbarLayout?.setOnScrimsListener(this)
    }

    override fun initData() {
        pagerAdapter?.let {
            for (i in 0 until it.count) {
                tabAdapter?.addItem(it.getPageTitle(i).toString())
            }
        }
        tabAdapter?.setOnTabListener(this)
    }

    override fun isStatusBarEnabled(): Boolean {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled()
    }

    override fun isStatusBarDarkFont(): Boolean {
        return collapsingToolbarLayout?.isScrimsShown() == true
    }

    /**
     * [TabAdapter.OnTabListener]
     */
    override fun onTabSelected(recyclerView: RecyclerView?, position: Int): Boolean {
        viewPager?.currentItem = position
        return true
    }

    /**
     * [ViewPager.OnPageChangeListener]
     */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        tabAdapter?.setSelectedPosition(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}

    /**
     * CollapsingToolbarLayout 渐变回调
     *
     * [XCollapsingToolbarLayout.OnScrimsListener]
     */
    @Suppress("RestrictedApi")
    override fun onScrimsStateChange(layout: XCollapsingToolbarLayout?, shown: Boolean) {
        getStatusBarConfig().statusBarDarkFont(shown).init()
        addressView?.setTextColor(ContextCompat.getColor(getAttachActivity()!!, if (shown) R.color.black else R.color.white))
        hintView?.setBackgroundResource(if (shown) R.drawable.home_search_bar_gray_bg else R.drawable.home_search_bar_transparent_bg)
        hintView?.setTextColor(ContextCompat.getColor(getAttachActivity()!!, if (shown) R.color.black60 else R.color.white60))
        searchView?.supportImageTintList = ColorStateList.valueOf(ContextCompat.getColor(getAttachActivity()!!,
            if (shown) R.color.common_icon_color else R.color.white))
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager?.adapter = null
        viewPager?.removeOnPageChangeListener(this)
        tabAdapter?.setOnTabListener(null)
    }
}