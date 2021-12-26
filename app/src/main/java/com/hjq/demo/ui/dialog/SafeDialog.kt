package com.hjq.demo.ui.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.http.api.GetCodeApi
import com.hjq.demo.http.api.VerifyCodeApi
import com.hjq.demo.http.model.HttpData
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.hjq.toast.ToastUtils
import com.hjq.widget.view.CountdownView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/02/06
 *    desc   : 身份校验对话框
 */
class SafeDialog {

    class Builder(context: Context) : CommonDialog.Builder<Builder>(context) {

        private val phoneView: TextView? by lazy { findViewById(R.id.tv_safe_phone) }
        private val codeView: EditText? by lazy { findViewById(R.id.et_safe_code) }
        private val countdownView: CountdownView? by lazy { findViewById(R.id.cv_safe_countdown) }

        private var listener: OnListener? = null

        /** 当前手机号 */
        private val phoneNumber: String

        init {
            setTitle(R.string.safe_title)
            setCustomView(R.layout.safe_dialog)
            setOnClickListener(countdownView)
            phoneNumber = "18100001413"
            // 为了保护用户的隐私，不明文显示中间四个数字
            phoneView?.text = String.format(
                "%s****%s", phoneNumber.substring(0, 3),
                phoneNumber.substring(phoneNumber.length - 4))
        }

        fun setCode(code: String?): Builder = apply {
            codeView?.setText(code)
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        @SingleClick
        override fun onClick(view: View) {
            when (view.id) {
                R.id.cv_safe_countdown -> {
                    if (true) {
                        ToastUtils.show(R.string.common_code_send_hint)
                        countdownView?.start()
                        setCancelable(false)
                        return
                    }

                    // 获取验证码
                    EasyHttp.post(getDialog())
                        .api(GetCodeApi().apply {
                            setPhone(phoneNumber)
                        })
                        .request(object : OnHttpListener<HttpData<Void?>?> {

                            override fun onSucceed(data: HttpData<Void?>?) {
                                ToastUtils.show(R.string.common_code_send_hint)
                                countdownView?.start()
                                setCancelable(false)
                            }

                            override fun onFail(e: Exception) {
                                ToastUtils.show(e.message)
                            }
                        })
                }
                R.id.tv_ui_confirm -> {
                    if (codeView?.text.toString().length != getResources().getInteger(R.integer.sms_code_length)) {
                        ToastUtils.show(R.string.common_code_error_hint)
                        return
                    }
                    if (true) {
                        autoDismiss()
                        listener?.onConfirm(getDialog(), phoneNumber, codeView?.text.toString())
                        return
                    }

                    // 验证码校验
                    EasyHttp.post(getDialog())
                        .api(VerifyCodeApi().apply {
                            setPhone(phoneNumber)
                            setCode(codeView?.text.toString())
                        })
                        .request(object : OnHttpListener<HttpData<Void?>?> {

                            override fun onSucceed(data: HttpData<Void?>?) {
                                autoDismiss()
                                listener?.onConfirm(getDialog(), phoneNumber, codeView?.text.toString())
                            }

                            override fun onFail(e: Exception) {
                                ToastUtils.show(e.message)
                            }
                        })
                }
                R.id.tv_ui_cancel -> {
                    autoDismiss()
                    listener?.onCancel(getDialog())
                }
            }
        }
    }

    interface OnListener {

        /**
         * 点击确定时回调
         */
        fun onConfirm(dialog: BaseDialog?, phone: String, code: String)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog?) {}
    }
}