package com.hjq.demo.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.widget.PasswordView
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/2
 *    desc   : 支付密码对话框
 */
class PayPasswordDialog {

    class Builder constructor(context: Context) : BaseDialog.Builder<Builder>(context),
        BaseAdapter.OnItemClickListener {

        companion object {

            /** 输入键盘文本 */
            private val KEYBOARD_TEXT: Array<String> =
                arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "")
        }

        private var listener: OnListener? = null
        private var autoDismiss: Boolean = true
        private val recordList: LinkedList<String?> = LinkedList()

        private val titleView: TextView? by lazy { findViewById(R.id.tv_pay_title) }
        private val closeView: ImageView? by lazy { findViewById(R.id.iv_pay_close) }
        private val subTitleView: TextView? by lazy { findViewById(R.id.tv_pay_sub_title) }
        private val moneyView: TextView? by lazy { findViewById(R.id.tv_pay_money) }
        private val passwordView: PasswordView? by lazy { findViewById(R.id.pw_pay_view) }
        private val recyclerView: RecyclerView? by lazy { findViewById(R.id.rv_pay_list) }
        private val adapter: KeyboardAdapter

        init {
            setContentView(R.layout.pay_password_dialog)
            setCancelable(false)
            setOnClickListener(closeView)
            adapter = KeyboardAdapter(getContext())
            adapter.setData(mutableListOf(*KEYBOARD_TEXT))
            adapter.setOnItemClickListener(this)
            recyclerView?.adapter = adapter
        }

        fun setTitle(@StringRes id: Int): Builder = apply {
            setTitle(getString(id))
        }

        fun setTitle(title: CharSequence?): Builder = apply {
            titleView?.text = title
        }

        fun setSubTitle(@StringRes id: Int): Builder = apply {
            setSubTitle(getString(id))
        }

        fun setSubTitle(subTitle: CharSequence?): Builder = apply {
            subTitleView?.text = subTitle
        }

        fun setMoney(@StringRes id: Int): Builder = apply {
            setMoney(getString(id))
        }

        fun setMoney(money: CharSequence?): Builder = apply {
            moneyView?.text = money
        }

        fun setAutoDismiss(dismiss: Boolean): Builder = apply {
            autoDismiss = dismiss
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        /**
         * [BaseAdapter.OnItemClickListener]
         */
        override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
            when (adapter.getItemViewType(position)) {
                KeyboardAdapter.TYPE_DELETE ->                     // 点击回退按钮删除
                    if (recordList.size != 0) {
                        recordList.removeLast()
                    }
                KeyboardAdapter.TYPE_EMPTY -> {}
                else -> {
                    // 判断密码是否已经输入完毕
                    if (recordList.size < PasswordView.PASSWORD_COUNT) {
                        // 点击数字，显示在密码行
                        recordList.add(KEYBOARD_TEXT[position])
                    }

                    // 判断密码是否已经输入完毕
                    if (recordList.size == PasswordView.PASSWORD_COUNT) {
                        postDelayed({
                            if (autoDismiss) {
                                dismiss()
                            }
                            // 获取输入的支付密码
                            val password: StringBuilder = StringBuilder()
                            for (s: String? in recordList) {
                                password.append(s)
                            }
                            listener?.onCompleted(getDialog(), password.toString())
                        }, 300)
                    }
                }
            }
            passwordView?.setPassWordLength(recordList.size)
        }

        @SingleClick
        override fun onClick(view: View) {
            if (view === closeView) {
                if (autoDismiss) {
                    dismiss()
                }
                listener?.onCancel(getDialog())
            }
        }
    }

    private class KeyboardAdapter(context: Context) :
        AppAdapter<String?>(context) {

        companion object {

            /** 数字按钮条目 */
            const val TYPE_NORMAL: Int = 0

            /** 删除按钮条目 */
            const val TYPE_DELETE: Int = 1

            /** 空按钮条目 */
            const val TYPE_EMPTY: Int = 2
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                9 -> TYPE_EMPTY
                11 -> TYPE_DELETE
                else -> TYPE_NORMAL
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            return when (viewType) {
                TYPE_DELETE -> SimpleViewHolder(R.layout.pay_password_delete_item)
                TYPE_EMPTY -> SimpleViewHolder(R.layout.pay_password_empty_item)
                else -> ViewHolder()
            }
        }

        private inner class ViewHolder : AppViewHolder(R.layout.pay_password_normal_item) {

            private val textView: TextView by lazy { getItemView() as TextView }

            override fun onBindView(position: Int) {
                textView.text = getItem(position)
            }
        }

        override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
            return GridLayoutManager(getContext(), 3)
        }
    }

    interface OnListener {

        /**
         * 输入完成时回调
         *
         * @param password      输入的密码
         */
        fun onCompleted(dialog: BaseDialog?, password: String)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog?) {}
    }
}