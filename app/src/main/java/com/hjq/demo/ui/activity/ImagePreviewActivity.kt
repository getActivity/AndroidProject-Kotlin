package com.hjq.demo.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.BaseAdapter
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.app.AppActivity
import com.hjq.demo.ui.adapter.ImagePreviewAdapter
import me.relex.circleindicator.CircleIndicator3
import java.util.*

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
            val images = ArrayList<String?>(1)
            images.add(url)
            start(context, images)
        }

        fun start(context: Context, urls: MutableList<String?>) {
            start(context, urls, 0)
        }

        @Log
        fun start(context: Context, urls: MutableList<String?>, index: Int) {
            var finalUrls: List<String?> = urls
            if (finalUrls.isEmpty()) {
                return
            }
            val intent = Intent(context, ImagePreviewActivity::class.java)
            if (finalUrls.size > 2000) {
                // 请注意：如果传输的数据量过大，会抛出此异常，并且这种异常是不能被捕获的
                // 所以当图片数量过多的时候，我们应当只显示一张，这种一般是手机图片过多导致的
                // 经过测试，传入 3121 张图片集合的时候会抛出此异常，所以保险值应当是 2000
                // android.os.TransactionTooLargeException: data parcel size 521984 bytes
                finalUrls = listOf(finalUrls[index])
            }
            if (finalUrls is ArrayList<*>) {
                intent.putExtra(INTENT_KEY_IN_IMAGE_LIST, finalUrls)
            } else {
                intent.putExtra(INTENT_KEY_IN_IMAGE_LIST, ArrayList(finalUrls))
            }
            intent.putExtra(INTENT_KEY_IN_IMAGE_INDEX, index)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val viewPager2: ViewPager2? by lazy { findViewById(R.id.vp_image_preview_pager) }
    private val adapter: ImagePreviewAdapter by lazy { ImagePreviewAdapter(this) }

    /** 圆圈指示器 */
    private val circleIndicatorView: CircleIndicator3? by lazy { findViewById(R.id.ci_image_preview_indicator) }

    /** 文本指示器 */
    private val textIndicatorView: TextView? by lazy { findViewById(R.id.tv_image_preview_indicator) }

    override fun getLayoutId(): Int {
        return R.layout.image_preview_activity
    }

    override fun initView() {
        viewPager2?.offscreenPageLimit = 3
    }

    override fun initData() {
        val images = getStringArrayList(INTENT_KEY_IN_IMAGE_LIST)
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
                viewPager2?.registerOnPageChangeCallback(mPageChangeCallback)
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
        viewPager2?.unregisterOnPageChangeCallback(mPageChangeCallback)
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     * @param recyclerView      RecyclerView 对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
        if (isFinishing || isDestroyed) {
            return
        }
        // 单击图片退出当前的 Activity
        finish()
    }

    /**
     * ViewPager2 页面改变监听器
     */
    private val mPageChangeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {

        @Suppress("SetTextI18n")
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            textIndicatorView?.text = (position + 1).toString() + "/" + adapter.getCount()
        }
    }
}