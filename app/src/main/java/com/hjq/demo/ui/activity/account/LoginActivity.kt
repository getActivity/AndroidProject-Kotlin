package com.hjq.demo.ui.activity.account

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
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
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.base.ktx.startActivity
import com.hjq.core.ktx.createIntent
import com.hjq.core.ktx.hideKeyboard
import com.hjq.core.manager.InputTextManager
import com.hjq.core.tools.KeyboardWatcher
import com.hjq.custom.widget.view.SubmitButton
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.LoginApi
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.ktx.toast
import com.hjq.demo.ui.activity.HomeActivity
import com.hjq.demo.ui.fragment.home.HomeMineFragment
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.config.IRequestApi
import com.hjq.http.listener.HttpCallbackProxy
import com.hjq.umeng.sdk.Platform
import com.hjq.umeng.sdk.UmengClient
import com.hjq.umeng.sdk.UmengLogin
import com.hjq.umeng.sdk.WXEntryActivity

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 登录界面
 */
class LoginActivity : AppActivity(), UmengLogin.OnLoginListener,
    KeyboardWatcher.SoftKeyboardStateListener, TextView.OnEditorActionListener {

    companion object {

        private const val INTENT_KEY_IN_PHONE: String = "phone"
        private const val INTENT_KEY_IN_PASSWORD: String = "password"

        @Log
        fun start(context: Context, phone: String?, password: String?) {
            val intent = context.createIntent(LoginActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_PHONE, phone)
            intent.putExtra(INTENT_KEY_IN_PASSWORD, password)
            context.startActivity(intent)
        }
    }

    /** logo 缩放比例 */
    private val logoScale: Float = 0.8f

    /** 动画时间 */
    private val animTime: Int = 300

    private val registerView: View? by lazyFindViewById(R.id.tv_login_register)
    private val logoView: ImageView? by lazyFindViewById(R.id.iv_login_logo)
    private val bodyLayout: ViewGroup? by lazyFindViewById(R.id.ll_login_body)
    private val phoneView: EditText? by lazyFindViewById(R.id.et_login_phone)
    private val passwordView: EditText? by lazyFindViewById(R.id.et_login_password)
    private val forgetView: View? by lazyFindViewById(R.id.tv_login_forget)
    private val commitView: SubmitButton? by lazyFindViewById(R.id.btn_login_commit)
    private val otherView: View? by lazyFindViewById(R.id.ll_login_other)
    private val qqView: View? by lazyFindViewById(R.id.iv_login_qq)
    private val weChatView: View? by lazyFindViewById(R.id.iv_login_wechat)

    override fun getLayoutId(): Int {
        return R.layout.login_activity
    }

    override fun initView() {
        setOnClickListener(registerView, forgetView, commitView, qqView, weChatView)
        passwordView?.setOnEditorActionListener(this)
        commitView?.let {
            InputTextManager.with(this)
                .addView(phoneView)
                .addView(passwordView)
                .setMain(it)
                .build()
        }
    }

    override fun initData() {
        postDelayed({
            KeyboardWatcher.with(this@LoginActivity)
                .setListener(this@LoginActivity)
        }, 500)

        // 判断用户当前有没有安装 QQ
        if (!UmengClient.isAppInstalled(this, Platform.QQ)) {
            qqView?.visibility = View.GONE
        }

        // 判断用户当前有没有安装微信
        if (!UmengClient.isAppInstalled(this, Platform.WECHAT)) {
            weChatView?.visibility = View.GONE
        }

        // 如果这两个都没有安装就隐藏提示
        if (qqView?.visibility == View.GONE && weChatView?.visibility == View.GONE) {
            otherView?.visibility = View.GONE
        }

        // 自动填充手机号和密码
        phoneView?.setText(getString(INTENT_KEY_IN_PHONE))
        passwordView?.setText(getString(INTENT_KEY_IN_PASSWORD))
    }

    override fun getImmersionTopView(): View? {
        return findViewById(R.id.fl_login_bar)
    }

    override fun getImmersionBottomView(): View? {
        return findViewById(R.id.ll_login_platform)
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view == registerView) {
            // 跳转到注册界面
            RegisterActivity.start(this, phoneView?.text.toString(), passwordView?.text.toString(),
                object : RegisterActivity.OnRegisterListener {

                    override fun onRegisterSuccess(phone: String, password: String) {
                        // 如果已经注册成功，就执行登录操作
                        phoneView?.setText(phone)
                        passwordView?.setText(password)
                        passwordView?.requestFocus()
                        passwordView?.setSelection(passwordView?.text.toString().length)
                        commitView?.let { onClick(it) }
                    }
                }
            )
            return
        }

        if (view === forgetView) {
            startActivity(PasswordForgetActivity::class.java)
            return
        }
        if (view === commitView) {
            if (phoneView?.text.toString().length != 11) {
                phoneView?.startAnimation(
                    AnimationUtils.loadAnimation(
                        getContext(),
                        R.anim.shake_anim
                    )
                )
                commitView?.showError(3000)
                toast(R.string.common_phone_input_error)
                return
            }

            // 隐藏软键盘
            hideKeyboard(currentFocus)
            if (true) {
                commitView?.showProgress()
                postDelayed({
                    commitView?.showSucceed()
                    postDelayed({
                        HomeActivity.start(getContext(), HomeMineFragment::class.java)
                        finish()
                    }, 1000)
                }, 2000)
                return
            }
            EasyHttp.post(this)
                .api(LoginApi().apply {
                    setPhone(phoneView?.text.toString())
                    setPassword(passwordView?.text.toString())
                })
                .request(object : HttpCallbackProxy<HttpData<LoginApi.Bean?>>(this) {

                    override fun onHttpStart(api: IRequestApi) {
                        commitView?.showProgress()
                    }

                    override fun onHttpEnd(api: IRequestApi) {
                        // default implementation ignored
                    }

                    override fun onHttpSuccess(data: HttpData<LoginApi.Bean?>) {
                        val bean = data.getData() ?: return
                        // 更新 Token
                        EasyConfig.getInstance().addHeader("token", bean.getToken())
                        postDelayed({
                            commitView?.showSucceed()
                            postDelayed({
                                // 跳转到首页
                                HomeActivity.start(getContext(), HomeMineFragment::class.java)
                                finish()
                            }, 1000)
                        }, 1000)
                    }

                    override fun onHttpFail(throwable: Throwable) {
                        super.onHttpFail(throwable)
                        postDelayed({ commitView?.showError(3000) }, 1000)
                    }
                })
            return
        }
        if (view === qqView || view === weChatView) {
            toast("记得改好第三方 AppID 和 Secret，否则会调不起来哦")
            val platform: Platform
            when {
                view === qqView -> {
                    platform = Platform.QQ
                }
                view === weChatView -> {
                    if (packageName.endsWith(".debug")) {
                        toast("当前 buildType 不支持进行微信登录")
                        return
                    }
                    platform = Platform.WECHAT
                    toast("也别忘了改微信 " + WXEntryActivity::class.java.simpleName + " 类所在的包名哦")
                }
                else -> {
                    throw IllegalStateException("are you ok?")
                }
            }
            UmengClient.login(this, platform, this)
        }
    }

    /**
     * [UmengLogin.OnLoginListener]
     */
    /**
     * 授权成功的回调
     *
     * @param platform      平台名称
     * @param data          用户资料返回
     */
    override fun onLoginSuccess(platform: Platform, data: UmengLogin.LoginData) {
        if (isFinishing || isDestroyed) {
            // Glide：You cannot start a load for a destroyed activity
            return
        }

        logoView?.let {
            GlideApp.with(this)
                .load(data?.getAvatar())
                .circleCrop()
                .into(it)
        }

        toast(("昵称：" + data?.getName() + "\n" +
                    "性别：" + data?.getSex() + "\n" +
                    "id：" + data?.getId() + "\n" +
                    "token：" + data?.getToken()))
    }

    /**
     * 授权失败的回调
     *
     * @param platform      平台名称
     * @param throwable             错误原因
     */
    override fun onLoginFail(platform: Platform, throwable: Throwable) {
        toast("第三方登录出错：" + throwable.message)
    }

    /**
     * [KeyboardWatcher.SoftKeyboardStateListener]
     */
    override fun onSoftKeyboardOpened(keyboardHeight: Int) {
        // 执行位移动画
        bodyLayout?.let {
            val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(it,
                "translationY", 0f, (-(commitView?.height?.toFloat() ?: 0f)))
            objectAnimator.duration = animTime.toLong()
            objectAnimator.interpolator = AccelerateDecelerateInterpolator()
            objectAnimator.start()
        }

        // 执行缩小动画
        logoView?.let {
            it.pivotX = it.width / 2f
            it.pivotY = it.height.toFloat()
            val animatorSet = AnimatorSet()
            val scaleX = ObjectAnimator.ofFloat(it, "scaleX", 1f, logoScale)
            val scaleY = ObjectAnimator.ofFloat(it, "scaleY", 1f, logoScale)
            val translationY = ObjectAnimator.ofFloat(it, "translationY",
                0f, (-(commitView?.height?.toFloat() ?: 0f)))
            animatorSet.play(translationY).with(scaleX).with(scaleY)
            animatorSet.duration = animTime.toLong()
            animatorSet.start()
        }
    }

    override fun onSoftKeyboardClosed() {
        // 执行位移动画
        bodyLayout?.let {
            val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(it,
                "translationY", it.translationY, 0f)
            objectAnimator.duration = animTime.toLong()
            objectAnimator.interpolator = AccelerateDecelerateInterpolator()
            objectAnimator.start()
        }

        // 执行放大动画
        logoView?.let {
            it.pivotX = it.width / 2f
            it.pivotY = it.height.toFloat()

            if (it.translationY == 0f) {
                return
            }

            val animatorSet = AnimatorSet()
            val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(it, "scaleX", logoScale, 1f)
            val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(it, "scaleY", logoScale, 1f)
            val translationY: ObjectAnimator = ObjectAnimator.ofFloat(it,
                "translationY", it.translationY, 0f)
            animatorSet.play(translationY).with(scaleX).with(scaleY)
            animatorSet.duration = animTime.toLong()
            animatorSet.start()
        }
    }

    /**
     * [TextView.OnEditorActionListener]
     */
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // 模拟点击提交按钮
            commitView?.let {
                if (it.isEnabled) {
                    // 模拟点击登录按钮
                    onClick(it)
                    return true
                }
            }
        }
        return false
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig()
            // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
    }
}