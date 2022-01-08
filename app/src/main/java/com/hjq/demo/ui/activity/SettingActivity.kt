package com.hjq.demo.ui.activity

import android.view.Gravity
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.hjq.base.BaseDialog
import com.hjq.base.action.AnimAction
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.LogoutApi
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.manager.CacheDataManager
import com.hjq.demo.other.AppConfig
import com.hjq.demo.ui.dialog.MenuDialog
import com.hjq.demo.ui.dialog.SafeDialog
import com.hjq.demo.ui.dialog.UpdateDialog
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.hjq.widget.layout.SettingBar
import com.hjq.widget.view.SwitchButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/03/01
 *    desc   : 设置界面
 */
class SettingActivity : AppActivity(), SwitchButton.OnCheckedChangeListener {

    private val languageView: SettingBar? by lazy { findViewById(R.id.sb_setting_language) }
    private val phoneView: SettingBar? by lazy { findViewById(R.id.sb_setting_phone) }
    private val passwordView: SettingBar? by lazy { findViewById(R.id.sb_setting_password) }
    private val cleanCacheView: SettingBar? by lazy { findViewById(R.id.sb_setting_cache) }
    private val autoSwitchView: SwitchButton? by lazy { findViewById(R.id.sb_setting_switch) }

    override fun getLayoutId(): Int {
        return R.layout.setting_activity
    }

    override fun initView() {
        // 设置切换按钮的监听
        autoSwitchView?.setOnCheckedChangeListener(this)
        setOnClickListener(R.id.sb_setting_language, R.id.sb_setting_update, R.id.sb_setting_phone,
            R.id.sb_setting_password, R.id.sb_setting_agreement, R.id.sb_setting_about,
            R.id.sb_setting_cache, R.id.sb_setting_auto, R.id.sb_setting_exit)
    }

    override fun initData() {
        // 获取应用缓存大小
        cleanCacheView?.setRightText(CacheDataManager.getTotalCacheSize(this))
        languageView?.setRightText("简体中文")
        phoneView?.setRightText("181****1413")
        passwordView?.setRightText("密码强度较低")
    }

    @SingleClick
    override fun onClick(view: View) {
        when (view.id) {
            R.id.sb_setting_language -> {

                // 底部选择框
                MenuDialog.Builder(this) // 设置点击按钮后不关闭对话框
                    //.setAutoDismiss(false)
                    .setList(R.string.setting_language_simple, R.string.setting_language_complex)
                    .setListener(object : MenuDialog.OnListener<String> {

                        override fun onSelected(dialog: BaseDialog?, position: Int, data: String) {
                            languageView?.setRightText(data)
                            BrowserActivity.start(this@SettingActivity, "https://github.com/getActivity/MultiLanguages")
                        }
                    })
                    .setGravity(Gravity.BOTTOM)
                    .setAnimStyle(AnimAction.ANIM_BOTTOM)
                    .show()
            }
            R.id.sb_setting_update -> {

                // 本地的版本码和服务器的进行比较
                if (20 > AppConfig.getVersionCode()) {
                    UpdateDialog.Builder(this)
                        .setVersionName("2.0")
                        .setForceUpdate(false)
                        .setUpdateLog("修复Bug\n优化用户体验")
                        .setDownloadUrl("https://down.qq.com/qqweb/QQ_1/android_apk/Android_8.5.0.5025_537066738.apk")
                        .setFileMd5("560017dc94e8f9b65f4ca997c7feb326")
                        .show()
                } else {
                    toast(R.string.update_no_update)
                }
            }
            R.id.sb_setting_phone -> {

                SafeDialog.Builder(this)
                    .setListener(object : SafeDialog.OnListener {

                        override fun onConfirm(dialog: BaseDialog?, phone: String, code: String) {
                            PhoneResetActivity.start(this@SettingActivity, code)
                        }
                    })
                    .show()
            }
            R.id.sb_setting_password -> {

                SafeDialog.Builder(this)
                    .setListener(object : SafeDialog.OnListener {

                        override fun onConfirm(dialog: BaseDialog?, phone: String, code: String) {
                            PasswordResetActivity.start(this@SettingActivity, phone, code)
                        }
                    })
                    .show()
            }
            R.id.sb_setting_agreement -> {

                BrowserActivity.start(this, "https://github.com/getActivity/Donate")
            }
            R.id.sb_setting_about -> {

                startActivity(AboutActivity::class.java)
            }
            R.id.sb_setting_auto -> {

                autoSwitchView?.let {
                    // 自动登录
                    it.setChecked(!it.isChecked())
                }
            }
            R.id.sb_setting_cache -> {

                // 清除内存缓存（必须在主线程）
                GlideApp.get(this@SettingActivity).clearMemory()
                lifecycleScope.launch(Dispatchers.IO) {
                    CacheDataManager.clearAllCache(this@SettingActivity)
                    // 清除本地缓存（必须在子线程）
                    GlideApp.get(this@SettingActivity).clearDiskCache()
                    withContext(Dispatchers.Main) {
                        // 重新获取应用缓存大小
                        cleanCacheView?.setRightText(CacheDataManager.getTotalCacheSize(this@SettingActivity))
                    }
                }
            }
            R.id.sb_setting_exit -> {

                if (true) {
                    startActivity(LoginActivity::class.java)
                    // 进行内存优化，销毁除登录页之外的所有界面
                    ActivityManager.getInstance().finishAllActivities(
                        LoginActivity::class.java
                    )
                    return
                }

                // 退出登录
                EasyHttp.post(this)
                    .api(LogoutApi())
                    .request(object : HttpCallback<HttpData<Void?>>(this) {

                        override fun onSucceed(data: HttpData<Void?>?) {
                            startActivity(LoginActivity::class.java)
                            // 进行内存优化，销毁除登录页之外的所有界面
                            ActivityManager.getInstance().finishAllActivities(LoginActivity::class.java)
                        }
                    })
            }
        }
    }

    /**
     * [SwitchButton.OnCheckedChangeListener]
     */
    override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
        toast(checked)
    }
}