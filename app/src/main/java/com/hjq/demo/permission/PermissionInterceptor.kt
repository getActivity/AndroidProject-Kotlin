package com.hjq.demo.permission

import android.app.Activity
import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.TextUtils
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.ktx.toast
import com.hjq.demo.permission.PermissionConverter.getNickNamesByPermissions
import com.hjq.demo.ui.dialog.common.MessageDialog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionInterceptor
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionGroups
import com.hjq.permissions.permission.PermissionNames
import com.hjq.permissions.permission.base.IPermission

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/10/24
 *    desc   : 权限申请拦截器
 */
class PermissionInterceptor : OnPermissionInterceptor {

    override fun onRequestPermissionEnd(activity: Activity, skipRequest: Boolean,
                                        requestList: List<IPermission>,
                                        grantedList: List<IPermission?>,
                                        deniedList: List<IPermission>,
                                        callback: OnPermissionCallback?) {

        callback?.onResult(grantedList, deniedList)

        if (deniedList.isEmpty()) {
            return
        }
        val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(activity, deniedList)
        val permissionHint = generatePermissionHint(activity, deniedList, doNotAskAgain)
        if (!doNotAskAgain) {
            // 如果没有勾选不再询问选项，就弹 Toast 提示给用户
            toast(permissionHint)
            return
        }

        // 如果勾选了不再询问选项，就弹 Dialog 引导用户去授权
        showPermissionSettingDialog(activity, requestList, deniedList, callback, permissionHint)
    }

    private fun showPermissionSettingDialog(
        activity: Activity,
        requestList: List<IPermission>,
        deniedList: List<IPermission>,
        callback: OnPermissionCallback?,
        permissionHint: String
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        MessageDialog.Builder(activity)
            .setTitle(R.string.common_permission_alert)
            .setMessage(permissionHint)
            .setConfirm(R.string.common_permission_go_to_authorization)
            .setListener(object : MessageDialog.OnListener {

                override fun onConfirm(dialog: BaseDialog?) {
                    dialog?.dismiss()
                    XXPermissions.startPermissionActivity(activity, deniedList) { _: List<IPermission>, _: List<IPermission> ->
                        val latestDeniedList = XXPermissions.getDeniedPermissions(activity, requestList)
                        val allGranted = latestDeniedList.isEmpty()
                        if (!allGranted) {
                            // 递归显示对话框，让提示用户授权，只不过对话框是可取消的，用户不想授权了，随时可以点击返回键或者对话框蒙层来取消显示
                            showPermissionSettingDialog(
                                activity, requestList, latestDeniedList, callback,
                                generatePermissionHint(activity, latestDeniedList, true)
                            )
                            return@startPermissionActivity
                        }

                        if (callback == null) {
                            return@startPermissionActivity
                        }
                        // 用户全部授权了，回调成功给外层监听器，免得用户还要再发起权限申请
                        callback.onResult(requestList, latestDeniedList)
                    }
                }
            })
            .show()
    }

    /**
     * 生成权限提示文案
     */
    private fun generatePermissionHint(activity: Activity, deniedList: List<IPermission>, doNotAskAgain: Boolean): String {
        val deniedPermissionCount = deniedList.size
        var deniedLocationPermissionCount = 0
        var deniedSensorsPermissionCount = 0
        var deniedHealthPermissionCount = 0
        for (deniedPermission in deniedList) {
            val permissionGroup = deniedPermission.getPermissionGroup(activity)
            if (TextUtils.isEmpty(permissionGroup)) {
                continue
            }
            if (PermissionGroups.LOCATION == permissionGroup) {
                deniedLocationPermissionCount++
            } else if (PermissionGroups.SENSORS == permissionGroup) {
                deniedSensorsPermissionCount++
            } else if (XXPermissions.isHealthPermission(deniedPermission)) {
                deniedHealthPermissionCount++
            }
        }

        if (deniedLocationPermissionCount == deniedPermissionCount && VERSION.SDK_INT >= VERSION_CODES.Q) {
            if (deniedLocationPermissionCount == 1) {
                if (XXPermissions.equalsPermission(deniedList[0], PermissionNames.ACCESS_BACKGROUND_LOCATION)) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_1,
                        activity.getString(R.string.common_permission_location_background),
                        getBackgroundPermissionOptionLabel(activity)
                    )
                } else if (VERSION.SDK_INT >= VERSION_CODES.S &&
                    XXPermissions.equalsPermission(deniedList[0], PermissionNames.ACCESS_FINE_LOCATION)
                ) {
                    // 如果请求的定位权限中，既包含了精确定位权限，又包含了模糊定位权限或者后台定位权限，
                    // 但是用户只同意了模糊定位权限的情况或者后台定位权限，并没有同意精确定位权限的情况，就提示用户开启确切位置选项
                    // 需要注意的是 Android 12 才将模糊定位权限和精确定位权限的授权选项进行分拆，之前的版本没有区分得那么仔细
                    return activity.getString(
                        R.string.common_permission_fail_hint_3,
                        activity.getString(R.string.common_permission_location_fine),
                        activity.getString(R.string.common_permission_location_fine_option)
                    )
                }
            } else {
                if (XXPermissions.containsPermission(deniedList, PermissionNames.ACCESS_BACKGROUND_LOCATION)) {
                    return if (VERSION.SDK_INT >= VERSION_CODES.S &&
                        XXPermissions.containsPermission(deniedList, PermissionNames.ACCESS_FINE_LOCATION)
                    ) {
                        activity.getString(
                            R.string.common_permission_fail_hint_2,
                            activity.getString(R.string.common_permission_location),
                            getBackgroundPermissionOptionLabel(activity),
                            activity.getString(R.string.common_permission_location_fine_option)
                        )
                    } else {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_location),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                }
            }
        } else if (deniedSensorsPermissionCount == deniedPermissionCount && VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            if (deniedPermissionCount == 1) {
                if (XXPermissions.equalsPermission(deniedList[0], PermissionNames.BODY_SENSORS_BACKGROUND)) {
                    return if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_health_data_background),
                            activity.getString(R.string.common_permission_health_data_background_option)
                        )
                    } else {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_body_sensors_background),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                }
            } else {
                if (doNotAskAgain) {
                    return if (VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_health_data),
                            activity.getString(R.string.common_permission_allow_all_option)
                        )
                    } else {
                        activity.getString(
                            R.string.common_permission_fail_hint_1,
                            activity.getString(R.string.common_permission_body_sensors),
                            getBackgroundPermissionOptionLabel(activity)
                        )
                    }
                }
            }
        } else if (deniedHealthPermissionCount == deniedPermissionCount && VERSION.SDK_INT >= VERSION_CODES.BAKLAVA) {
            when (deniedPermissionCount) {
                1 -> if (XXPermissions.equalsPermission(deniedList[0], PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND)) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_3,
                        activity.getString(R.string.common_permission_health_data_background),
                        activity.getString(R.string.common_permission_health_data_background_option)
                    )
                } else if (XXPermissions.equalsPermission(deniedList[0], PermissionNames.READ_HEALTH_DATA_HISTORY)) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_3,
                        activity.getString(R.string.common_permission_health_data_past),
                        activity.getString(R.string.common_permission_health_data_past_option)
                    )
                }

                2 -> if (XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_HISTORY) &&
                    XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND)
                ) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_3,
                        activity.getString(R.string.common_permission_health_data_past) + activity.getString(R.string.common_permission_and) + activity.getString(
                            R.string.common_permission_health_data_background
                        ),
                        activity.getString(R.string.common_permission_health_data_past_option) + activity.getString(R.string.common_permission_and) + activity.getString(
                            R.string.common_permission_health_data_background_option
                        )
                    )
                } else if (XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_HISTORY)) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_2,
                        activity.getString(R.string.common_permission_health_data) + activity.getString(R.string.common_permission_and) + activity.getString(
                            R.string.common_permission_health_data_past
                        ),
                        activity.getString(R.string.common_permission_allow_all_option),
                        activity.getString(R.string.common_permission_health_data_background_option)
                    )
                } else if (XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND)) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_2,
                        activity.getString(R.string.common_permission_health_data) + activity.getString(R.string.common_permission_and) + activity.getString(
                            R.string.common_permission_health_data_background
                        ),
                        activity.getString(R.string.common_permission_allow_all_option),
                        activity.getString(R.string.common_permission_health_data_background_option)
                    )
                }

                else -> if (XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_HISTORY) &&
                    XXPermissions.containsPermission(deniedList, PermissionNames.READ_HEALTH_DATA_IN_BACKGROUND)
                ) {
                    return activity.getString(
                        R.string.common_permission_fail_hint_2,
                        activity.getString(R.string.common_permission_health_data) + activity.getString(R.string.common_permission_and) + activity.getString(
                            R.string.common_permission_health_data_past
                        ) + activity.getString(R.string.common_permission_and) + activity.getString(R.string.common_permission_health_data_background),
                        activity.getString(R.string.common_permission_allow_all_option),
                        activity.getString(R.string.common_permission_health_data_past_option) + activity.getString(R.string.common_permission_and) + activity.getString(
                            R.string.common_permission_health_data_background_option
                        )
                    )
                }
            }
            return activity.getString(
                R.string.common_permission_fail_hint_1,
                activity.getString(R.string.common_permission_health_data),
                activity.getString(R.string.common_permission_allow_all_option)
            )
        }

        return activity.getString(
            if (doNotAskAgain) R.string.common_permission_fail_assign_hint_1 else R.string.common_permission_fail_assign_hint_2,
            getNickNamesByPermissions(activity, deniedList)
        )
    }

    /**
     * 获取后台权限的《始终允许》选项的文案
     */
    private fun getBackgroundPermissionOptionLabel(context: Context): String {
        val packageManager = context.packageManager
        if (packageManager != null && VERSION.SDK_INT >= VERSION_CODES.R) {
            val backgroundPermissionOptionLabel = packageManager.backgroundPermissionOptionLabel
            if (!TextUtils.isEmpty(backgroundPermissionOptionLabel)) {
                return backgroundPermissionOptionLabel.toString()
            }
        }
        return context.getString(R.string.common_permission_allow_all_the_time_option)
    }
}