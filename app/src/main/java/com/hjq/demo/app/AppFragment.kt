package com.hjq.demo.app

import com.hjq.base.BaseFragment
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.ktx.toast
import com.hjq.http.config.IRequestApi
import com.hjq.http.listener.OnHttpListener

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/10/18
 *    desc   : Fragment 业务基类
 */
abstract class AppFragment<A : AppActivity> : BaseFragment<A>(), OnHttpListener<Any> {

    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        val activity: A = getAttachActivity() ?: return false
        return activity.isShowDialog()
    }

    /**
     * 显示加载对话框
     */
    open fun showLoadingDialog() {
        getAttachActivity()?.showLoadingDialog()
    }

    open fun showLoadingDialog(message: String) {
        getAttachActivity()?.showLoadingDialog(message)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideLoadingDialog() {
        getAttachActivity()?.hideLoadingDialog()
    }

    /**
     * [OnHttpListener]
     */
    override fun onHttpStart(api: IRequestApi) {
        showLoadingDialog()
    }

    override fun onHttpSuccess(result: Any) {
        if (result !is HttpData<*>) {
            return
        }
        toast(result.getMessage())
    }

    override fun onHttpFail(throwable: Throwable) {
        toast(throwable.message)
    }

    override fun onHttpEnd(api: IRequestApi) {
        hideLoadingDialog()
    }
}