package com.hjq.demo.ui.activity

import android.view.*
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.GetCodeApi
import com.hjq.demo.http.api.VerifyCodeApi
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.manager.InputTextManager
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.hjq.widget.view.CountdownView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/02/27
 *    desc   : 忘记密码
 */
class PasswordForgetActivity : AppActivity(), OnEditorActionListener {

    private val phoneView: EditText? by lazy { findViewById(R.id.et_password_forget_phone) }
    private val codeView: EditText? by lazy { findViewById(R.id.et_password_forget_code) }
    private val countdownView: CountdownView? by lazy { findViewById(R.id.cv_password_forget_countdown) }
    private val commitView: Button? by lazy { findViewById(R.id.btn_password_forget_commit) }

    override fun getLayoutId(): Int {
        return R.layout.password_forget_activity
    }

    override fun initView() {
        setOnClickListener(countdownView, commitView)
        codeView?.setOnEditorActionListener(this)
        commitView?.let {
            InputTextManager.with(this)
                .addView(phoneView)
                .addView(codeView)
                .setMain(it)
                .build()
        }
    }

    override fun initData() {}

    @SingleClick
    override fun onClick(view: View) {
        if (view === countdownView) {
            if (phoneView?.text.toString().length != 11) {
                phoneView?.startAnimation(
                    AnimationUtils.loadAnimation(
                        getContext(),
                        R.anim.shake_anim
                    )
                )
                toast(R.string.common_phone_input_error)
                return
            }
            if (true) {
                toast(R.string.common_code_send_hint)
                countdownView?.start()
                return
            }

            // 隐藏软键盘
            hideKeyboard(currentFocus)

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
                })

        } else if (view === commitView) {

            if (phoneView?.text.toString().length != 11) {
                phoneView?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                toast(R.string.common_phone_input_error)
                return
            }

            if (codeView?.text.toString().length != resources.getInteger(R.integer.sms_code_length)) {
                codeView?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                toast(R.string.common_code_error_hint)
                return
            }
            if (true) {
                PasswordResetActivity.start(this, phoneView?.text.toString(), codeView?.text.toString())
                finish()
                return
            }

            // 验证码校验
            EasyHttp.post(this)
                .api(VerifyCodeApi().apply {
                    setPhone(phoneView?.text.toString())
                    setCode(codeView?.text.toString())
                })
                .request(object : HttpCallback<HttpData<Void?>>(this) {

                    override fun onSucceed(data: HttpData<Void?>) {
                        PasswordResetActivity.start(this@PasswordForgetActivity,
                            phoneView?.text.toString(), codeView?.text.toString())
                        finish()
                    }
                })
        }
    }

    /**
     * [TextView.OnEditorActionListener]
     */
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // 模拟点击下一步按钮
            commitView?.let {
                if (it.isEnabled) {
                    onClick(it)
                    return true
                }
            }
        }
        return false
    }
}