package com.hjq.demo.ui.activity.common

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.hjq.base.ktx.createIntent
import com.hjq.demo.R
import com.hjq.demo.app.AppActivity
import com.hjq.demo.ui.activity.HomeActivity
import com.hjq.demo.ui.activity.SplashActivity

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/11/29
 *    desc   : 重启应用
 */
class RestartActivity : AppActivity() {

    companion object {
        fun start(context: Context) {
            val intent = context.createIntent(RestartActivity::class.java)
            context.startActivity(intent)
        }

        fun restart(context: Context) {
            val intent: Intent = if (true) {
                // 如果是未登录的情况下跳转到闪屏页
                context.createIntent(SplashActivity::class.java)
            } else {
                // 如果是已登录的情况下跳转到首页
                context.createIntent(HomeActivity::class.java)
            }
            context.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun initView() {
        // 这里解释一下，为什么不用 Toaster 来显示，而是用系统的来显示
        // 这是因为 Application 在初始化第三方框架前会判断当前是否是主进程
        // 如果是主进程才会初始化第三方框架，但是当前 Activity 运行在非主进程中
        Toast.makeText(this, R.string.common_crash_hint, Toast.LENGTH_LONG).show()
    }

    override fun initData() {
        restart(this)
        finish()
    }
}