package com.hjq.demo.ui.fragment.home

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hjq.base.ktx.dp2px
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.TitleBarFragment
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.ktx.toast
import com.hjq.demo.ui.activity.HomeActivity
import com.hjq.widget.view.CountdownView
import com.hjq.widget.view.SimpleRatingBar
import com.hjq.widget.view.SwitchButton

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 发现 Fragment
 */
class HomeFindFragment : TitleBarFragment<HomeActivity>(),
    SwitchButton.OnCheckedChangeListener,
    SimpleRatingBar.OnRatingChangeListener {

    companion object {

        fun newInstance(): HomeFindFragment {
            return HomeFindFragment()
        }
    }

    private val circleView: ImageView? by lazyFindViewById(R.id.iv_home_find_circle)
    private val cornerView: ImageView? by lazyFindViewById(R.id.iv_home_find_corner)
    private val switchButton: SwitchButton? by lazyFindViewById(R.id.sb_home_find_switch)
    private val countdownView: CountdownView? by lazyFindViewById(R.id.cv_home_find_countdown)

    override fun getLayoutId(): Int {
        return R.layout.home_find_fragment
    }

    override fun initView() {
        setOnClickListener(countdownView)
        switchButton?.setOnCheckedChangeListener(this)

        val simpleRatingBar1 = findViewById<SimpleRatingBar>(R.id.srb_home_find_rating_bar_1)
        simpleRatingBar1!!.setOnRatingBarChangeListener(this)
        val simpleRatingBar2 = findViewById<SimpleRatingBar>(R.id.srb_home_find_rating_bar_2)
        simpleRatingBar2!!.setOnRatingBarChangeListener(this)
    }

    override fun initData() {
        circleView?.let {
            // 显示圆形的 ImageView
            GlideApp.with(this)
                .load(R.drawable.update_app_top_bg)
                .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                .into(it)
        }

        cornerView?.let {
            // 显示圆角的 ImageView
            GlideApp.with(this)
                .load(R.drawable.update_app_top_bg)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(dp2px(10).toInt())))
                .into(it)
        }
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view === countdownView) {
            toast(R.string.common_code_send_hint)
            countdownView?.start()
        }
    }

    override fun isStatusBarEnabled(): Boolean {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled()
    }

    /**
     * [SwitchButton.OnCheckedChangeListener]
     */
    override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
        toast(checked)
    }

    /**
     * [SimpleRatingBar.OnRatingChangeListener]
     */
    override fun onRatingChanged(ratingBar: SimpleRatingBar, grade: Float, touch: Boolean) {
        toast(grade)
    }
}