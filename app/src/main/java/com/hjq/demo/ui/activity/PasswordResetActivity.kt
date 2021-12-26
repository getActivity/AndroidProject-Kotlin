package com.hjq.demo.ui.activity

import android.app.Activity
import android.content.*
import android.view.*
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.PasswordApi
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.manager.InputTextManager
import com.hjq.demo.ui.dialog.TipsDialog
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/02/27
 *    desc   : 重置密码
 */
class PasswordResetActivity : AppActivity(), OnEditorActionListener {

    companion object {

        private const val INTENT_KEY_IN_PHONE: String = "phone"
        private const val INTENT_KEY_IN_CODE: String = "code"

        @Log
        fun start(context: Context, phone: String?, code: String?) {
            val intent = Intent(context, PasswordResetActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_PHONE, phone)
            intent.putExtra(INTENT_KEY_IN_CODE, code)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val firstPassword: EditText? by lazy { findViewById(R.id.et_password_reset_password1) }
    private val secondPassword: EditText? by lazy { findViewById(R.id.et_password_reset_password2) }
    private val commitView: Button? by lazy { findViewById(R.id.btn_password_reset_commit) }

    /** 手机号 */
    private var phoneNumber: String? = null

    /** 验证码 */
    private var verifyCode: String? = null

    override fun getLayoutId(): Int {
        return R.layout.password_reset_activity
    }

    override fun initView() {
        setOnClickListener(commitView)
        secondPassword?.setOnEditorActionListener(this)
        commitView?.let {
            InputTextManager.with(this)
                .addView(firstPassword)
                .addView(secondPassword)
                .setMain(it)
                .build()
        }
    }

    override fun initData() {
        phoneNumber = getString(INTENT_KEY_IN_PHONE)
        verifyCode = getString(INTENT_KEY_IN_CODE)
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view === commitView) {
            if (firstPassword?.text.toString() != secondPassword?.text.toString()) {
                firstPassword?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                secondPassword?.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim))
                toast(R.string.common_password_input_unlike)
                return
            }

            // 隐藏软键盘
            hideKeyboard(currentFocus)
            if (true) {
                TipsDialog.Builder(this)
                    .setIcon(TipsDialog.ICON_FINISH)
                    .setMessage(R.string.password_reset_success)
                    .setDuration(2000)
                    .addOnDismissListener(object : BaseDialog.OnDismissListener {

                        override fun onDismiss(dialog: BaseDialog?) {
                            finish()
                        }
                    })
                    .show()
                return
            }

            // 重置密码
            EasyHttp.post(this)
                .api(PasswordApi().apply {
                    setPhone(phoneNumber)
                    setCode(verifyCode)
                    setPassword(firstPassword?.text.toString())
                })
                .request(object : HttpCallback<HttpData<Void?>>(this) {

                    override fun onSucceed(data: HttpData<Void?>) {
                        TipsDialog.Builder(this@PasswordResetActivity)
                            .setIcon(TipsDialog.ICON_FINISH)
                            .setMessage(R.string.password_reset_success)
                            .setDuration(2000)
                            .addOnDismissListener(object : BaseDialog.OnDismissListener {

                                override fun onDismiss(dialog: BaseDialog?) {
                                    finish()
                                }
                            })
                            .show()
                    }
                })
        }
    }

    /**
     * [TextView.OnEditorActionListener]
     */
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // 模拟点击提交按钮
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