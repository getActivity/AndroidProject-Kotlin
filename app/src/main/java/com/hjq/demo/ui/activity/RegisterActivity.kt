package com.hjq.demo.ui.activity

import android.content.Intent
import android.view.*
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.gyf.immersionbar.ImmersionBar
import com.hjq.base.BaseActivity
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.GetCodeApi
import com.hjq.demo.http.api.RegisterApi
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.manager.InputTextManager
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.hjq.widget.view.CountdownView
import com.hjq.widget.view.SubmitButton
import okhttp3.Call

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
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.putExtra(INTENT_KEY_PHONE, phone)
            intent.putExtra(INTENT_KEY_PASSWORD, password)
            activity.startActivityForResult(intent, object : OnActivityCallback {

                override fun onActivityResult(resultCode: Int, data: Intent?) {
                    if (listener == null || data == null) {
                        return
                    }
                    if (resultCode == RESULT_OK) {
                        listener.onSucceed(data.getStringExtra(INTENT_KEY_PHONE), data.getStringExtra(INTENT_KEY_PASSWORD))
                    } else {
                        listener.onCancel()
                    }
                }
            })
        }
    }

    private val phoneView: EditText? by lazy { findViewById(R.id.et_register_phone) }
    private val countdownView: CountdownView? by lazy { findViewById(R.id.cv_register_countdown) }
    private val codeView: EditText? by lazy { findViewById(R.id.et_register_code) }
    private val firstPassword: EditText? by lazy { findViewById(R.id.et_register_password1) }
    private val secondPassword: EditText? by lazy { findViewById(R.id.et_register_password2) }
    private val commitView: SubmitButton? by lazy { findViewById(R.id.btn_register_commit) }

    override fun getLayoutId(): Int {
        return R.layout.register_activity
    }

    override fun initView() {
        setOnClickListener(countdownView, commitView)
        secondPassword?.setOnEditorActionListener(this)

        // 给这个 View 设置沉浸式，避免状态栏遮挡
        ImmersionBar.setTitleBar(this, findViewById(R.id.tv_register_title))
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
                .request(object : HttpCallback<HttpData<Void?>>(this) {

                    override fun onSucceed(data: HttpData<Void?>) {
                        toast(R.string.common_code_send_hint)
                        countdownView?.start()
                    }

                    override fun onFail(e: Exception?) {
                        super.onFail(e)
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

            if (codeView?.text.toString().length != resources.getInteger(R.integer.sms_code_length)) {
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
                .request(object : HttpCallback<HttpData<RegisterApi.Bean?>>(this) {

                    override fun onStart(call: Call) {
                        commitView?.showProgress()
                    }

                    override fun onEnd(call: Call) {}

                    override fun onSucceed(data: HttpData<RegisterApi.Bean?>) {
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

                    override fun onFail(e: Exception?) {
                        super.onFail(e)
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
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
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
        fun onSucceed(phone: String?, password: String?)

        /**
         * 取消注册
         */
        fun onCancel() {}
    }
}