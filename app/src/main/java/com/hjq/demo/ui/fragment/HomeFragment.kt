package com.hjq.demo.ui.fragment

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.FragmentPagerAdapter
import com.hjq.demo.R
import com.hjq.demo.app.AppFragment
import com.hjq.demo.app.TitleBarFragment
import com.hjq.demo.ui.activity.HomeActivity
import com.hjq.demo.ui.adapter.TabAdapter
import com.hjq.demo.ui.adapter.TabAdapter.OnTabListener
import com.hjq.demo.widget.XCollapsingToolbarLayout
import com.hjq.demo.widget.XCollapsingToolbarLayout.OnScrimsListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 首页 Fragment
 */
class HomeFragment : TitleBarFragment<HomeActivity>(), OnTabListener,
    OnPageChangeListener, OnScrimsListener {

    companion object {

        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private val collapsingToolbarLayout: XCollapsingToolbarLayout? by lazy { findViewById(R.id.ctl_home_bar) }
    private val toolbar: Toolbar? by lazy { findViewById(R.id.tb_home_title) }
    private val addressView: TextView? by lazy { findViewById(R.id.tv_home_address) }
    private val hintView: TextView? by lazy { findViewById(R.id.tv_home_hint) }
    private val searchView: AppCompatImageView? by lazy { findViewById(R.id.iv_home_search) }
    private val tabView: RecyclerView? by lazy { findViewById(R.id.rv_home_tab) }
    private val viewPager: ViewPager? by lazy { findViewById(R.id.vp_home_pager) }

    private var tabAdapter: TabAdapter? = null
    private var pagerAdapter: FragmentPagerAdapter<AppFragment<*>>? = null

    override fun getLayoutId(): Int {
        return R.layout.home_fragment
    }

    override fun initView() {
        pagerAdapter = FragmentPagerAdapter(this)
        pagerAdapter!!.addFragment(StatusFragment.newInstance(), "列表演示")
        pagerAdapter!!.addFragment(BrowserFragment.newInstance("https://github.com/getActivity"), "网页演示")
        viewPager?.adapter = pagerAdapter
        viewPager?.addOnPageChangeListener(this)
        tabAdapter = TabAdapter(getAttachActivity()!!)
        tabView?.adapter = tabAdapter

        // 给这个 ToolBar 设置顶部内边距，才能和 TitleBar 进行对齐
        ImmersionBar.setTitleBar(getAttachActivity(), toolbar)

        // 设置渐变监听
        collapsingToolbarLayout?.setOnScrimsListener(this)
    }

    override fun initData() {
        tabAdapter?.addItem("列表演示")
        tabAdapter?.addItem("网页演示")
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