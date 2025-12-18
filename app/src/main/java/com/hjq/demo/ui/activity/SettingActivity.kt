package com.hjq.demo.ui.activity

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.hjq.base.BaseDialog
import com.hjq.base.action.AnimAction
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.base.ktx.startActivity
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.api.LogoutApi
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.http.model.HttpData
import com.hjq.demo.ktx.toast
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.manager.CacheDataManager
import com.hjq.demo.other.AppConfig
import com.hjq.demo.ui.activity.account.LoginActivity
import com.hjq.demo.ui.activity.account.PasswordResetActivity
import com.hjq.demo.ui.activity.account.PhoneResetActivity
import com.hjq.demo.ui.activity.common.BrowserActivity
import com.hjq.demo.ui.dialog.SafeDialog
import com.hjq.demo.ui.dialog.UpdateDialog
import com.hjq.demo.ui.dialog.common.MenuDialog
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
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

    private val changeLanguageView: SettingBar? by lazyFindViewById(R.id.sb_setting_change_language)
    private val checkUpdateView: SettingBar? by lazyFindViewById(R.id.sb_setting_check_update)
    private val modifyPhoneView: SettingBar? by lazyFindViewById(R.id.sb_setting_modify_phone)
    private val modifyPasswordView: SettingBar? by lazyFindViewById(R.id.sb_setting_modify_password)
    private val readAgreementView: SettingBar? by lazyFindViewById(R.id.sb_setting_read_agreement)
    private val aboutAppView: SettingBar? by lazyFindViewById(R.id.sb_setting_about_app)
    private val autoLoginView: SettingBar? by lazyFindViewById(R.id.sb_setting_auto_login)
    private val cleanCacheView: SettingBar? by lazyFindViewById(R.id.sb_setting_clear_cache)
    private val exitLoginView: SettingBar? by lazyFindViewById(R.id.sb_setting_exit_login)

    private val autoSwitchView: SwitchButton? by lazyFindViewById(R.id.sb_setting_switch)

    override fun getLayoutId(): Int {
        return R.layout.setting_activity
    }

    override fun initView() {
        // 适配 RTL 特性
        val iconDrawable: Drawable? = if (getResources().configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            getDrawable(R.drawable.arrows_left_ic)
        } else {
            getDrawable(R.drawable.arrows_right_ic)
        }
        changeLanguageView?.setEndDrawable(iconDrawable)
        modifyPhoneView?.setEndDrawable(iconDrawable)
        modifyPasswordView?.setEndDrawable(iconDrawable)
        readAgreementView?.setEndDrawable(iconDrawable)
        aboutAppView?.setEndDrawable(iconDrawable)
        autoLoginView?.setEndDrawable(iconDrawable)
        cleanCacheView?.setEndDrawable(iconDrawable)
        exitLoginView?.setEndDrawable(iconDrawable)

        // 设置切换按钮的监听
        autoSwitchView?.setOnCheckedChangeListener(this)
        setOnClickListener(changeLanguageView, checkUpdateView, modifyPhoneView, modifyPasswordView,
                           readAgreementView, aboutAppView, autoLoginView, cleanCacheView, exitLoginView)
    }

    override fun initData() {
        // 获取应用缓存大小
        cleanCacheView?.setEndText(CacheDataManager.getTotalCacheSize(this))
        changeLanguageView?.setEndText("简体中文")
        modifyPhoneView?.setEndText("181****1413")
        modifyPasswordView?.setEndText("密码强度较低")
    }

    override fun getImmersionBottomView(): View? {
        return findViewById(R.id.ll_setting_content)
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view === changeLanguageView) {

            // 底部选择框
            MenuDialog.Builder(this) // 设置点击按钮后不关闭对话框
                //.setAutoDismiss(false)
                .setList(R.string.setting_language_simple, R.string.setting_language_complex)
                .setListener(object : MenuDialog.OnListener<String> {

                    override fun onSelected(dialog: BaseDialog, position: Int, data: String) {
                        changeLanguageView?.setEndText(data)
                        BrowserActivity.start(this@SettingActivity, "https://github.com/getActivity/MultiLanguages")
                    }
                })
                .setGravity(Gravity.BOTTOM)
                .setAnimStyle(AnimAction.ANIM_BOTTOM)
                .show()

        } else if (view === checkUpdateView) {

            // 本地的版本码和服务器的进行比较
            if (20 > AppConfig.getVersionCode()) {
                UpdateDialog.Builder(this)
                    .setVersionName("2.0")
                    .setForceUpdate(false)
                    .setUpdateLog("修复Bug\n优化用户体验")
                    .setDownloadUrl("https://dldir1.qq.com/weixin/android/weixin8015android2020_arm64.apk")
                    .setFileMd5("b05b25d4738ea31091dd9f80f4416469")
                    .show()
            } else {
                toast(R.string.update_no_update)
            }

        } else if (view === modifyPhoneView) {

            SafeDialog.Builder(this)
                .setListener(object : SafeDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog, phone: String, code: String) {
                        PhoneResetActivity.start(this@SettingActivity, code)
                    }
                })
                .show()

        } else if (view === modifyPasswordView) {

            SafeDialog.Builder(this)
                .setListener(object : SafeDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog, phone: String, code: String) {
                        PasswordResetActivity.start(this@SettingActivity, phone, code)
                    }
                })
                .show()

        } else if (view === readAgreementView) {

            BrowserActivity.start(this, "https://github.com/getActivity/Donate")

        } else if (view === aboutAppView) {

            startActivity(AboutActivity::class.java)

        } else if (view === autoLoginView) {

            autoSwitchView?.let {
                // 自动登录
                it.setChecked(!it.isChecked())
            }

        } else if (view === cleanCacheView) {

            // 清除内存缓存（必须在主线程）
            GlideApp.get(this@SettingActivity).clearMemory()
            lifecycleScope.launch(Dispatchers.IO) {
                CacheDataManager.clearAllCache(this@SettingActivity)
                // 清除本地缓存（必须在子线程）
                GlideApp.get(this@SettingActivity).clearDiskCache()
                withContext(Dispatchers.Main) {
                    // 重新获取应用缓存大小
                    cleanCacheView?.setEndText(CacheDataManager.getTotalCacheSize(this@SettingActivity))
                }
            }

        } else if (view === exitLoginView) {

            if (true) {
                startActivity(LoginActivity::class.java)
                // 进行内存优化，销毁除登录页之外的所有界面
                ActivityManager.finishAllActivities(LoginActivity::class.java)
                return
            }

            // 退出登录
            EasyHttp.post(this)
                .api(LogoutApi())
                .request(object : HttpCallbackProxy<HttpData<Any>>(this) {

                    override fun onHttpSuccess(data: HttpData<Any>) {
                        startActivity(LoginActivity::class.java)
                        // 进行内存优化，销毁除登录页之外的所有界面
                        ActivityManager.finishAllActivities(LoginActivity::class.java)
                    }
                })
        }
    }

    /**
     * [SwitchButton.OnCheckedChangeListener]
     */
    override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
        toast(checked)
    }
}