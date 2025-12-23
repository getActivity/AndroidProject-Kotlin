package com.hjq.demo.ui.activity.account

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.BaseActivity
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.base.ktx.startActivityForResult
import com.hjq.core.ktx.createIntent
import com.hjq.core.ktx.hideKeyboard
import com.hjq.core.manager.InputTextManager
import com.hjq.custom.widget.view.CountdownView
import com.hjq.custom.widget.view.SubmitButton
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.GetCodeApi
import com.hjq.demo.http.api.RegisterApi
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.ktx.toast
import com.hjq.http.EasyHttp
import com.hjq.http.config.IRequestApi
import com.hjq.http.listener.HttpCallbackProxy

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : 注册界面
 */
class RegisterActivity : AppActivity(), OnEditorActionListener {

    companion object {

        private const val INTENT_KEY_PHONE: String = "phone"
        private const val INTENT_KEY_PASSWORD: String = "password"

        @Log
        fun start(activity: BaseActivity, phone: String?, password: String?, listener: OnRegisterListener?) {
            val intent = activity.createIntent(RegisterActivity::class.java)
            intent.putExtra(INTENT_KEY_PHONE, phone)
            intent.putExtra(INTENT_KEY_PASSWORD, password)
            activity.startActivityForResult(RegisterActivity::class.java, {
                intent.putExtra(INTENT_KEY_PHONE, phone)
                intent.putExtra(INTENT_KEY_PASSWORD, password)
            }, OnActivityCallback { resultCode, data ->
                if (data == null) {
                    return@OnActivityCallback
                }
                if (resultCode == RESULT_OK) {
                    listener?.onRegisterSuccess(data.getStringExtra(INTENT_KEY_PHONE) ?: "",
                                            data.getStringExtra(INTENT_KEY_PASSWORD) ?: "")
                } else {
                    listener?.onRegisterCancel()
                }
            })
        }
    }

    private val phoneView: EditText? by lazyFindViewById(R.id.et_register_phone)
    private val countdownView: CountdownView? by lazyFindViewById(R.id.cv_register_countdown)
    private val codeView: EditText? by lazyFindViewById(R.id.et_register_code)
    private val firstPassword: EditText? by lazyFindViewById(R.id.et_register_password1)
    private val secondPassword: EditText? by lazyFindViewById(R.id.et_register_password2)
    private val commitView: SubmitButton? by lazyFindViewById(R.id.btn_register_commit)

    override fun getLayoutId(): Int {
        return R.layout.register_activity
    }

    override fun initView() {
        setOnClickListener(countdownView, commitView)
        secondPassword?.setOnEditorActionListener(this)

        commitView?.let {
            InputTextManager.with(this)
                .addView(phoneView)
                .addView(codeView)
                .addView(firstPassword)
                .addView(secondPassword)
                .setMain(it)
                .build()
        }
    }

    override fun initData() {
        // 自动填充手机号和密码
        phoneView?.setText(getString(INTENT_KEY_PHONE))
        firstPassword?.setText(getString(INTENT_KEY_PASSWORD))
        secondPassword?.setText(getString(INTENT_KEY_PASSWORD))
    }

    override fun getImmersionTopView(): View? {
        return findViewById(R.id.fl_register_container)
    }

    override fun getImmersionBottomView(): View? {
        return findViewById(R.id.fl_register_container)
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view === countdownView) {

            if (phoneView?.text.toString().length != 11) {
                phoneView?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                toast(R.string.common_phone_input_error)
                return
            }

            if (true) {
                toast(R.string.common_code_send_hint)
                countdownView?.start()
                return
            }

            // 获取验证码
            EasyHttp.post(this)
                .api(GetCodeApi().apply {
                    setPhone(phoneView?.text.toString())
                })
                .request(object : HttpCallbackProxy<HttpData<Any>>(this) {

                    override fun onHttpSuccess(data: HttpData<Any>) {
                        toast(R.string.common_code_send_hint)
                        countdownView?.start()
                    }

                    override fun onHttpFail(throwable: Throwable) {
                        super.onHttpFail(throwable)
                        countdownView?.start()
                    }
                })

        } else if (view === commitView) {

            if (phoneView?.text.toString().length != 11) {
                phoneView?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                commitView?.showError(3000)
                toast(R.string.common_phone_input_error)
                return
            }

            if (codeView?.text.toString().length != resources.getInteger(R.integer.sms_code_max_length)) {
                codeView?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                commitView?.showError(3000)
                toast(R.string.common_code_error_hint)
                return
            }

            if (firstPassword?.text.toString() != secondPassword?.text.toString()) {
                firstPassword?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                secondPassword?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                commitView?.showError(3000)
                toast(R.string.common_password_input_unlike)
                return
            }

            // 隐藏软键盘
            hideKeyboard(currentFocus)
            if (true) {
                commitView?.showProgress()
                postDelayed({
                    commitView?.showSucceed()
                    postDelayed({
                        setResult(RESULT_OK, Intent()
                                .putExtra(INTENT_KEY_PHONE, phoneView?.text.toString())
                                .putExtra(INTENT_KEY_PASSWORD, firstPassword?.text.toString()))
                        finish()
                    }, 1000)
                }, 2000)
                return
            }

            // 提交注册
            EasyHttp.post(this)
                .api(RegisterApi().apply {
                    setPhone(phoneView?.text.toString())
                    setCode(codeView?.text.toString())
                    setPassword(firstPassword?.text.toString())
                })
                .request(object : HttpCallbackProxy<HttpData<RegisterApi.Bean?>>(this) {

                    override fun onHttpStart(api: IRequestApi) {
                        commitView?.showProgress()
                    }

                    override fun onHttpEnd(api: IRequestApi) {
                        // default implementation ignored
                    }

                    override fun onHttpSuccess(data: HttpData<RegisterApi.Bean?>) {
                        postDelayed({
                            commitView?.showSucceed()
                            postDelayed({
                                setResult(RESULT_OK, Intent()
                                        .putExtra(INTENT_KEY_PHONE, phoneView?.text.toString())
                                        .putExtra(INTENT_KEY_PASSWORD, firstPassword?.text.toString()))
                                finish()
                            }, 1000)
                        }, 1000)
                    }

                    override fun onHttpFail(throwable: Throwable) {
                        super.onHttpFail(throwable)
                        postDelayed({ commitView?.showError(3000) }, 1000)
                    }
                })
        }
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig() // 指定导航栏背景颜色
            .navigationBarColor(R.color.white) // 不要把整个布局顶上去
            .keyboardEnable(true)
    }

    /**
     * [TextView.OnEditorActionListener]
     */
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // 模拟点击注册按钮
            commitView?.let {
                if (it.isEnabled) {
                    onClick(it)
                }
            }
            return true
        }
        return false
    }

    /**
     * 注册监听
     */
    interface OnRegisterListener {

        /**
         * 注册成功
         *
         * @param phone             手机号
         * @param password          密码
         */
        fun onRegisterSuccess(phone: String, password: String)

        /**
         * 取消注册
         */
        fun onRegisterCancel() {
            // default implementation ignored
        }
    }
}