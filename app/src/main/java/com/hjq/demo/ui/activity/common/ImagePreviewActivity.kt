package com.hjq.demo.ui.activity.common

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.BaseAdapter
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.core.ktx.createIntent
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.app.AppActivity
import com.hjq.demo.ui.adapter.common.ImagePreviewAdapter
import me.relex.circleindicator.CircleIndicator3

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/03/05
 *    desc   : 查看大图
 */
class ImagePreviewActivity : AppActivity(), BaseAdapter.OnItemClickListener {

    companion object {

        private const val INTENT_KEY_IN_IMAGE_LIST = "imageList"
        private const val INTENT_KEY_IN_IMAGE_INDEX = "imageIndex"

        fun start(context: Context, url: String) {
            val images: MutableList<String> = mutableListOf()
            images.add(url)
            start(context, images)
        }

        fun start(context: Context, urls: MutableList<String>) {
            start(context, urls, 0)
        }

        @Log
        fun start(context: Context, urls: MutableList<String>, index: Int) {
            var finalUrls: MutableList<String> = urls
            if (finalUrls.isEmpty()) {
                return
            }
            val intent = context.createIntent(ImagePreviewActivity::class.java)
            if (finalUrls.size > 2000) {
                // 请注意：如果传输的数据量过大，会抛出此异常，并且这种异常是不能被捕获的
                // 所以当图片数量过多的时候，我们应当只显示一张，这种一般是手机图片过多导致的
                // 经过测试，传入 3121 张图片集合的时候会抛出此异常，所以保险值应当是 2000
                // android.os.TransactionTooLargeException: data parcel size 521984 bytes
                finalUrls = mutableListOf(finalUrls[index])
            }
            intent.putExtra(INTENT_KEY_IN_IMAGE_LIST, finalUrls.toCollection(ArrayList()))
            intent.putExtra(INTENT_KEY_IN_IMAGE_INDEX, index)
            context.startActivity(intent)
        }
    }

    private val viewPager2: ViewPager2? by lazyFindViewById(R.id.vp_image_preview_pager)
    private val adapter: ImagePreviewAdapter by lazy { ImagePreviewAdapter(this) }

    /** 圆圈指示器 */
    private val circleIndicatorView: CircleIndicator3? by lazyFindViewById(R.id.ci_image_preview_indicator)

    /** 文本指示器 */
    private val textIndicatorView: TextView? by lazyFindViewById(R.id.tv_image_preview_indicator)

    /** ViewPager2 页面改变监听器 */
    private val pageChangeCallback: OnPageChangeCallback by lazy { object : OnPageChangeCallback() {

        @Suppress("SetTextI18n")
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            // 适配 RTL 特性
            textIndicatorView?.text = if (adapter.getContext().resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                adapter.getCount().toString() + "/" + (position + 1)
            } else {
                (position + 1).toString() + "/" + adapter.getCount()
            }
        }
    }}

    override fun getLayoutId(): Int {
        return R.layout.image_preview_activity
    }

    override fun initView() {
        viewPager2?.offscreenPageLimit = 3
    }

    override fun initData() {
        val images: MutableList<String?>? = getStringArrayList(INTENT_KEY_IN_IMAGE_LIST)
        if (images == null || images.isEmpty()) {
            finish()
            return
        }
        adapter.setData(images)
        adapter.setOnItemClickListener(this)
        viewPager2?.adapter = adapter

        if (images.size != 1) {
            if (images.size < 10) {
                // 如果是 10 张以内的图片，那么就显示圆圈指示器
                circleIndicatorView?.visibility = View.VISIBLE
                circleIndicatorView?.setViewPager(viewPager2)
            } else {
                // 如果超过 10 张图片，那么就显示文字指示器
                textIndicatorView?.visibility = View.VISIBLE
                pageChangeCallback.onPageSelected(0)
                viewPager2?.registerOnPageChangeCallback(pageChangeCallback)
            }
            val index = getInt(INTENT_KEY_IN_IMAGE_INDEX)
            if (index < images.size) {
                viewPager2?.setCurrentItem(index, false)
            }
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig() // 隐藏状态栏和导航栏
            .hideBar(BarHide.FLAG_HIDE_BAR)
    }

    override fun isStatusBarDarkFont(): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager2?.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     * @param recyclerView      RecyclerView 对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    override fun onItemClick(recyclerView: RecyclerView, itemView: View, position: Int) {
        if (isFinishing || isDestroyed) {
            return
        }
        // 单击图片退出当前的 Activity
        finish()
    }
}