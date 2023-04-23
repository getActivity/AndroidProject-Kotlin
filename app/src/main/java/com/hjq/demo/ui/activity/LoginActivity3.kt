package com.hjq.demo.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.gyf.immersionbar.ImmersionBar
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.LoginApi
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.manager.InputTextManager
import com.hjq.demo.other.KeyboardWatcher
import com.hjq.demo.ui.fragment.MineFragment
import com.hjq.demo.wxapi.WXEntryActivity
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.hjq.umeng.Platform
import com.hjq.umeng.UmengClient
import com.hjq.umeng.UmengLogin
import com.hjq.widget.view.SubmitButton
import okhttp3.Call

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 登录界面
 */
class LoginActivity3 : AppActivity() {


    override fun getLayoutId(): Int {
        return R.layout.login_activity1
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun onRightClick(view: View) {

    }


    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig()
            // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
    }
}