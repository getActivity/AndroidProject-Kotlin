package com.hjq.demo.ui.activity

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gyf.immersionbar.ImmersionBar
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.ui.adapter.GuideAdapter
import me.relex.circleindicator.CircleIndicator3

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/21
 *    desc   : 应用引导页
 */
class GuideActivity : AppActivity() {

    private val viewPager: ViewPager2? by lazy { findViewById(R.id.vp_guide_pager) }
    private val indicatorView: CircleIndicator3? by lazy { findViewById(R.id.cv_guide_indicator) }
    private val completeView: View? by lazy { findViewById(R.id.btn_guide_complete) }

    private val adapter: GuideAdapter = GuideAdapter(this)

    override fun getLayoutId(): Int {
        return R.layout.guide_activity
    }

    override fun initView() {
        setOnClickListener(completeView)
    }

    override fun initData() {
        adapter.addItem(R.drawable.guide_1_bg)
        adapter.addItem(R.drawable.guide_2_bg)
        adapter.addItem(R.drawable.guide_3_bg)

        viewPager?.adapter = adapter
        viewPager?.registerOnPageChangeCallback(mCallback)
        indicatorView?.setViewPager(viewPager)
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view === completeView) {
            HomeActivity.start(getContext())
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager?.unregisterOnPageChangeCallback(mCallback)
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig()
            // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
    }

    private val mCallback: OnPageChangeCallback = object : OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (viewPager?.currentItem != adapter.getCount() - 1 || positionOffsetPixels <= 0) {
                return
            }
            indicatorView?.visibility = View.VISIBLE
            completeView?.visibility = View.INVISIBLE
            completeView?.clearAnimation()
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state != ViewPager2.SCROLL_STATE_IDLE) {
                return
            }
            val lastItem: Boolean = viewPager?.currentItem == adapter.getCount() - 1
            indicatorView?.visibility = if (lastItem) View.INVISIBLE else View.VISIBLE
            completeView?.visibility = if (lastItem) View.VISIBLE else View.INVISIBLE
            if (lastItem) {
                // 按钮呼吸动效
                val animation = ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                )
                animation.duration = 350
                animation.repeatMode = Animation.REVERSE
                animation.repeatCount = Animation.INFINITE
                completeView?.startAnimation(animation)
            }
        }
    }
}