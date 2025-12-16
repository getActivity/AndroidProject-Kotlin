package com.hjq.demo.ui.fragment.home

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hjq.base.ktx.dp2px
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.TitleBarFragment
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.ktx.toast
import com.hjq.demo.permission.PermissionDescription
import com.hjq.demo.permission.PermissionInterceptor
import com.hjq.demo.ui.activity.HomeActivity
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 消息 Fragment
 */
class HomeMessageFragment : TitleBarFragment<HomeActivity>() {

    companion object {

        fun newInstance(): HomeMessageFragment {
            return HomeMessageFragment()
        }
    }

    private val imageView: ImageView? by lazyFindViewById(R.id.iv_home_message_image)

    override fun getLayoutId(): Int {
        return R.layout.home_message_fragment
    }

    override fun initView() {
        setOnClickListener(
            R.id.btn_home_message_image1, R.id.btn_home_message_image2, R.id.btn_home_message_image3,
            R.id.btn_home_message_toast, R.id.btn_home_message_permission, R.id.btn_home_message_setting,
            R.id.btn_home_message_black, R.id.btn_home_message_white, R.id.btn_home_message_tab
        )
    }

    override fun initData() {
        // default implementation ignored
    }

    override fun isStatusBarEnabled(): Boolean {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled()
    }

    @SingleClick
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_home_message_image1 -> {

                imageView?.let {
                    it.visibility = View.VISIBLE
                    GlideApp.with(this)
                        .load("https://www.baidu.com/img/bd_logo.png")
                        .into(it)
                }
            }
            R.id.btn_home_message_image2 -> {

                imageView?.let {
                    it.visibility = View.VISIBLE
                    GlideApp.with(this)
                        .load("https://www.baidu.com/img/bd_logo.png")
                        .circleCrop()
                        .into(it)
                }
            }
            R.id.btn_home_message_image3 -> {

                imageView?.let {
                    it.visibility = View.VISIBLE
                    GlideApp.with(this)
                        .load("https://www.baidu.com/img/bd_logo.png")
                        .transform(RoundedCorners(dp2px(20).toInt()))
                        .into(it)
                }
            }
            R.id.btn_home_message_toast -> {

                toast("我是吐司")

            }
            R.id.btn_home_message_permission -> {

                requestPermission()
            }
            R.id.btn_home_message_setting -> {

                XXPermissions.startPermissionActivity(this)
            }
            R.id.btn_home_message_black -> {

                getStatusBarConfig()
                    .statusBarDarkFont(true)
                    .init()
            }
            R.id.btn_home_message_white -> {

                getStatusBarConfig()
                    .statusBarDarkFont(false)
                    .init()
            }
            R.id.btn_home_message_tab -> {

                HomeActivity.start(view.context, HomeMainFragment::class.java)
            }
        }
    }

    private fun requestPermission() {
        XXPermissions.with(requireContext())
            .permission(PermissionLists.getCameraPermission())
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request { _, deniedList ->
                val allGranted = deniedList.isEmpty()
                if (!allGranted) {
                    return@request
                }
                toast("获取相机权限成功")
            }
    }
}