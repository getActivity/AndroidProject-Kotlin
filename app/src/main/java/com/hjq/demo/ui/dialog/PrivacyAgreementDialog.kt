package com.hjq.demo.ui.dialog

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.widget.TextView
import com.hjq.demo.R
import com.hjq.demo.other.LinkClickableSpan
import com.hjq.demo.ui.dialog.common.MessageDialog

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2023/06/24
 *    desc   : 用户协议与隐私政策弹窗
 */
class PrivacyAgreementDialog {

    class Builder(context: Context) : MessageDialog.Builder(context) {

        init {
            setCancelable(false)
            setCanceledOnTouchOutside(false)

            setTitle(getString(R.string.privacy_agreement_title))
            setConfirm(getString(R.string.privacy_agreement_agree))
            setCancel(getString(R.string.privacy_agreement_disagree))

            val privacyAgreementContent: String = getString(R.string.privacy_agreement_content) ?: ""
            val spannable = SpannableStringBuilder(privacyAgreementContent)

            val userAgreement: String = getString(R.string.privacy_agreement_user_agreement_text) ?: ""
            val userAgreementTextStart = privacyAgreementContent.indexOf(userAgreement)
            val userAgreementTextEnd = userAgreementTextStart + userAgreement.length
            if (userAgreementTextStart != -1 && userAgreementTextEnd < privacyAgreementContent.length) {
                spannable.setSpan(LinkClickableSpan(getString(R.string.privacy_agreement_user_agreement_link) ?: ""),
                    userAgreementTextStart, userAgreementTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val privacyPolicy: String = getString(R.string.privacy_agreement_privacy_policy_text) ?: ""
            val privacyPolicyTextStart = privacyAgreementContent.indexOf(privacyPolicy)
            val privacyPolicyTextEnd = privacyPolicyTextStart + privacyPolicy.length
            if (privacyPolicyTextStart != -1 && privacyPolicyTextEnd < privacyAgreementContent.length) {
                spannable.setSpan(LinkClickableSpan(getString(R.string.privacy_agreement_privacy_policy_link) ?: ""),
                    privacyPolicyTextStart, privacyPolicyTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val messageView: TextView? = messageView
            messageView?.gravity = Gravity.START
            messageView?.movementMethod = LinkMovementMethod.getInstance()

            setMessage(spannable)
        }
    }
}