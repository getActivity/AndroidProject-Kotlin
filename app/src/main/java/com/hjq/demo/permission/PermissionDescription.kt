package com.hjq.demo.permission

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.permission.PermissionConverter.getDescriptionsByPermissions
import com.hjq.demo.ui.dialog.common.MessageDialog
import com.hjq.demo.ui.popup.PermissionDescriptionPopup
import com.hjq.permissions.OnPermissionDescription
import com.hjq.permissions.permission.PermissionPageType
import com.hjq.permissions.permission.base.IPermission
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/XXPermissions
 *    time   : 2025/05/30
 *    desc   : 权限请求描述实现
 */
class PermissionDescription : OnPermissionDescription {

    /** 权限请求描述弹窗显示类型  */
    private var descriptionWindowType = DESCRIPTION_WINDOW_TYPE_DIALOG

    /** 消息 Token  */
    private val handlerToken = Any()

    /** 权限申请说明弹窗  */
    private var permissionPopupWindow: PopupWindow? = null

    /** 权限申请说明对话框  */
    private var permissionDialog: Dialog? = null

    override fun askWhetherRequestPermission(activity: Activity, requestList: MutableList<IPermission>,
                                             continueRequestRunnable: Runnable, breakRequestRunnable: Runnable) {
        // 以下情况使用 Dialog 来展示权限说明弹窗，否则使用 PopupWindow 来展示权限说明弹窗
        // 1. 如果请求的权限显示的系统界面是不透明的 Activity
        // 2. 如果当前 Activity 的屏幕是横屏状态的话，要求物理尺寸要够大，否则显示的顶部弹窗会被遮挡住，
        //    设备的物理屏幕尺寸还小于 8.5 寸（目前大多数小屏平板大多数集中在 8、8.7、8.8、10 寸），
        //    实测 8 寸的平板获取到的物理尺寸到只有 7.958788793906728，所以这里的代码判断基本上是针对 8.5 寸及以上的平板做优化。
        if (isActivityLandscape(activity) && getPhysicalScreenSize(activity) < 8.5) {
            descriptionWindowType = DESCRIPTION_WINDOW_TYPE_DIALOG
        } else {
            descriptionWindowType = DESCRIPTION_WINDOW_TYPE_POPUP
            for (permission in requestList) {
                if (permission.getPermissionPageType(activity) == PermissionPageType.OPAQUE_ACTIVITY) {
                    descriptionWindowType = DESCRIPTION_WINDOW_TYPE_DIALOG
                }
            }
        }

        if (descriptionWindowType == DESCRIPTION_WINDOW_TYPE_POPUP) {
            continueRequestRunnable.run()
            return
        }

        showDialog(activity, activity.getString(R.string.common_permission_description_title),
            generatePermissionDescription(activity, requestList),
            activity.getString(R.string.common_permission_confirm), object : MessageDialog.OnListener {
                override fun onConfirm(dialog: BaseDialog) {
                    dialog.dismiss()
                    continueRequestRunnable.run()
                }
            })
    }

    override fun onRequestPermissionStart(activity: Activity, requestList: MutableList<IPermission>) {
        if (descriptionWindowType != DESCRIPTION_WINDOW_TYPE_POPUP) {
            return
        }

        val showPopupRunnable =
            Runnable { showPopupWindow(activity, generatePermissionDescription(activity, requestList)) }
        // 这里解释一下为什么要延迟一段时间再显示 PopupWindow，这是因为系统没有开放任何 API 给外层直接获取权限是否永久拒绝
        // 目前只有申请过了权限才能通过 shouldShowRequestPermissionRationale 判断是不是永久拒绝，如果此前没有申请过权限，则无法判断
        // 针对这个问题能想到最佳的解决方案是：先申请权限，如果极短的时间内，权限申请没有结束，则证明权限之前没有被用户勾选了《不再询问》
        // 此时系统的权限弹窗正在显示给用户，这个时候再去显示应用的 PopupWindow 权限说明弹窗给用户看，所以这个 PopupWindow 是在发起权限申请后才显示的
        // 这样做是为了避免 PopupWindow 显示了又马上消失，这样就不会出现 PopupWindow 一闪而过的效果，提升用户的视觉体验
        // 最后补充一点：350 毫秒只是一个经验值，经过测试可覆盖大部分机型，具体可根据实际情况进行调整，这里不做强制要求
        // 相关 Github issue 地址：https://github.com/getActivity/XXPermissions/issues/366
        HANDLER.postAtTime(showPopupRunnable, handlerToken, SystemClock.uptimeMillis() + 350)
    }

    override fun onRequestPermissionEnd(activity: Activity, requestList: MutableList<IPermission>) {
        // 移除跟这个 Token 有关但是没有还没有执行的消息
        HANDLER.removeCallbacksAndMessages(handlerToken)
        // 销毁当前正在显示的弹窗
        dismissPopupWindow()
        dismissDialog()
    }

    /**
     * 生成权限描述文案
     */
    private fun generatePermissionDescription(activity: Activity, requestList: MutableList<IPermission>): String {
        return getDescriptionsByPermissions(activity, requestList)
    }

    /**
     * 显示 Dialog
     *
     * @param dialogTitle               对话框标题
     * @param dialogMessage             对话框消息
     * @param confirmButtonText         对话框确认按钮文本
     * @param listener                  对话框监听事件
     */
    private fun showDialog(activity: Activity, dialogTitle: String?, dialogMessage: String?, confirmButtonText: String?, listener: MessageDialog.OnListener?) {
        if (permissionDialog != null) {
            dismissDialog()
        }
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        // 另外这里需要判断 Activity 的类型来申请权限，这是因为只有 AppCompatActivity 才能调用 AndroidX 库的 AlertDialog 来显示，否则会出现报错
        // java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity
        // 为什么不直接用系统包 AlertDialog 来显示，而是两套规则？因为系统包 AlertDialog 是系统自带的类，不同 Android 版本展现的样式可能不太一样
        // 如果这个 Android 版本比较低，那么这个对话框的样式就会变得很丑，准确来讲也不能说丑，而是当时系统的 UI 设计就是那样，它只是跟随系统的样式而已
        permissionDialog = MessageDialog.Builder(activity)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setConfirm(confirmButtonText)
            .setCancelable(false)
            .setListener(listener)
            .create()
        permissionDialog?.show()
    }

    /**
     * 销毁 Dialog
     */
    private fun dismissDialog() {
        if (permissionDialog == null) {
            return
        }
        if (permissionDialog?.isShowing != true) {
            return
        }
        permissionDialog?.dismiss()
        permissionDialog = null
    }

    /**
     * 显示 PopupWindow
     *
     * @param content               弹窗显示的内容
     */
    private fun showPopupWindow(activity: Activity, content: String) {
        if (permissionPopupWindow != null) {
            dismissPopupWindow()
        }
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        val decorView = activity.window.decorView as ViewGroup
        permissionPopupWindow = PermissionDescriptionPopup.Builder(activity)
            .setDescription(content)
            .create()
        permissionPopupWindow?.showAtLocation(decorView, Gravity.TOP, 0, 0)
    }

    /**
     * 销毁 PopupWindow
     */
    private fun dismissPopupWindow() {
        if (permissionPopupWindow == null) {
            return
        }
        if (permissionPopupWindow?.isShowing != true) {
            return
        }
        permissionPopupWindow?.dismiss()
        permissionPopupWindow = null
    }

    companion object {
        /** 消息处理 Handler 对象  */
        val HANDLER: Handler = Handler(Looper.getMainLooper())

        /** 权限请求描述弹窗显示类型：Dialog  */
        private const val DESCRIPTION_WINDOW_TYPE_DIALOG = 0

        /** 权限请求描述弹窗显示类型：PopupWindow  */
        private const val DESCRIPTION_WINDOW_TYPE_POPUP = 1

        /**
         * 判断当前 Activity 是否是横盘显示
         */
        fun isActivityLandscape(activity: Activity): Boolean {
            return activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

        /**
         * 获取当前设备的物理屏幕尺寸
         */
        @Suppress("deprecation")
        fun getPhysicalScreenSize(context: Context): Float {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val defaultDisplay = windowManager.defaultDisplay ?: return 0f

            val metrics = DisplayMetrics()
            defaultDisplay.getMetrics(metrics)

            val screenWidthInInches: Float
            val screenHeightInInches: Float
            val point = Point()
            defaultDisplay.getRealSize(point)
            screenWidthInInches = point.x / metrics.xdpi
            screenHeightInInches = point.y / metrics.ydpi

            // 勾股定理：直角三角形的两条直角边的平方和等于斜边的平方
            return sqrt(screenWidthInInches.pow(2.0f) + screenHeightInInches.pow(2.0f))
        }
    }
}