package com.hjq.demo.ui.activity

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.other.AppConfig
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/06/27
 *    desc   : 崩溃捕捉界面
 */
class CrashActivity : AppActivity() {

    companion object {

        private const val INTENT_KEY_IN_THROWABLE: String = "throwable"

        /** 系统包前缀列表 */
        private val SYSTEM_PACKAGE_PREFIX_LIST: Array<String> = arrayOf("android", "com.android",
            "androidx", "com.google.android", "java", "javax", "dalvik", "kotlin")

        /** 报错代码行数正则表达式 */
        private val CODE_REGEX: Pattern = Pattern.compile("\\(\\w+\\.\\w+:\\d+\\)")

        fun start(application: Application, throwable: Throwable?) {
            if (throwable == null) {
                return
            }
            val intent = Intent(application, CrashActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_THROWABLE, throwable)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        }
    }

    private val titleView: TextView? by lazy { findViewById(R.id.tv_crash_title) }
    private val drawerLayout: DrawerLayout? by lazy { findViewById(R.id.dl_crash_drawer) }
    private val infoView: TextView? by lazy { findViewById(R.id.tv_crash_info) }
    private val messageView: TextView? by lazy { findViewById(R.id.tv_crash_message) }
    private var stackTrace: String? = null

    override fun getLayoutId(): Int {
        return R.layout.crash_activity
    }

    override fun initView() {
        setOnClickListener(R.id.iv_crash_info, R.id.iv_crash_share, R.id.iv_crash_restart)

        // 设置状态栏沉浸
        ImmersionBar.setTitleBar(this, findViewById(R.id.ll_crash_bar))
        ImmersionBar.setTitleBar(this, findViewById(R.id.ll_crash_info))
    }

    override fun initData() {
        val throwable: Throwable = getSerializable(INTENT_KEY_IN_THROWABLE) ?: return
        titleView?.text = throwable.javaClass.simpleName
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        throwable.cause?.printStackTrace(printWriter)
        stackTrace = stringWriter.toString()
        val matcher: Matcher = CODE_REGEX.matcher(stackTrace!!)
        val spannable = SpannableStringBuilder(stackTrace)
        if (spannable.isNotEmpty()) {
            while (matcher.find()) {
                // 不包含左括号（
                val start: Int = matcher.start() + "(".length
                // 不包含右括号 ）
                val end: Int = matcher.end() - ")".length

                // 代码信息颜色
                var codeColor: Int = Color.parseColor("#999999")
                val lineIndex: Int = stackTrace!!.lastIndexOf("at ", start)
                if (lineIndex != -1) {
                    val lineData: String = spannable.subSequence(lineIndex, start).toString()
                    if (TextUtils.isEmpty(lineData)) {
                        continue
                    }
                    // 是否高亮代码行数
                    var highlight = true
                    for (packagePrefix: String? in SYSTEM_PACKAGE_PREFIX_LIST) {
                        if (lineData.startsWith("at $packagePrefix")) {
                            highlight = false
                            break
                        }
                    }
                    if (highlight) {
                        codeColor = Color.parseColor("#287BDE")
                    }
                }

                // 设置前景
                spannable.setSpan(ForegroundColorSpan(codeColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                // 设置下划线
                spannable.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            messageView?.text = spannable
        }
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidth: Int = displayMetrics.widthPixels
        val screenHeight: Int = displayMetrics.heightPixels
        val smallestWidth: Float = min(screenWidth, screenHeight) / displayMetrics.density
        val targetResource: String?
        when {
            displayMetrics.densityDpi > 480 -> {
                targetResource = "xxxhdpi"
            }
            displayMetrics.densityDpi > 320 -> {
                targetResource = "xxhdpi"
            }
            displayMetrics.densityDpi > 240 -> {
                targetResource = "xhdpi"
            }
            displayMetrics.densityDpi > 160 -> {
                targetResource = "hdpi"
            }
            displayMetrics.densityDpi > 120 -> {
                targetResource = "mdpi"
            }
            else -> {
                targetResource = "ldpi"
            }
        }
        val builder: StringBuilder = StringBuilder()
        builder.append("设备品牌：\t").append(Build.BRAND)
            .append("\n设备型号：\t").append(Build.MODEL)
            .append("\n设备类型：\t").append(if (isTablet()) "平板" else "手机")

        builder.append("\n屏幕宽高：\t").append(screenWidth).append(" x ").append(screenHeight)
            .append("\n屏幕密度：\t").append(displayMetrics.densityDpi)
            .append("\n密度像素：\t").append(displayMetrics.density)
            .append("\n目标资源：\t").append(targetResource)
            .append("\n最小宽度：\t").append(smallestWidth.toInt())

        builder.append("\n安卓版本：\t").append(Build.VERSION.RELEASE)
            .append("\nAPI 版本：\t").append(Build.VERSION.SDK_INT)
            .append("\nCPU 架构：\t").append(Build.SUPPORTED_ABIS[0])

        builder.append("\n应用版本：\t").append(AppConfig.getVersionName())
            .append("\n版本代码：\t").append(AppConfig.getVersionCode())

        try {
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            builder.append("\n首次安装：\t")
                .append(dateFormat.format(Date(packageInfo.firstInstallTime)))
                .append("\n最近安装：\t").append(dateFormat.format(Date(packageInfo.lastUpdateTime)))
                .append("\n崩溃时间：\t").append(dateFormat.format(Date()))
            val permissions: MutableList<String> = mutableListOf(*packageInfo.requestedPermissions)
            if (permissions.contains(Permission.READ_EXTERNAL_STORAGE) ||
                permissions.contains(Permission.WRITE_EXTERNAL_STORAGE)) {
                builder.append("\n存储权限：\t").append(
                    if (XXPermissions.isGranted(this, *Permission.Group.STORAGE)) "已获得" else "未获得"
                )
            }
            if (permissions.contains(Permission.ACCESS_FINE_LOCATION) ||
                permissions.contains(Permission.ACCESS_COARSE_LOCATION)) {
                builder.append("\n定位权限：\t")
                if (XXPermissions.isGranted(this, Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)) {
                    builder.append("精确、粗略")
                } else {
                    when {
                        XXPermissions.isGranted(this, Permission.ACCESS_FINE_LOCATION) -> {
                            builder.append("精确")
                        }
                        XXPermissions.isGranted(this, Permission.ACCESS_COARSE_LOCATION) -> {
                            builder.append("粗略")
                        }
                        else -> {
                            builder.append("未获得")
                        }
                    }
                }
            }
            if (permissions.contains(Permission.CAMERA)) {
                builder.append("\n相机权限：\t")
                    .append(if (XXPermissions.isGranted(this, Permission.CAMERA)) "已获得" else "未获得")
            }
            if (permissions.contains(Permission.RECORD_AUDIO)) {
                builder.append("\n录音权限：\t").append(
                    if (XXPermissions.isGranted(this, Permission.RECORD_AUDIO)) "已获得" else "未获得"
                )
            }
            if (permissions.contains(Permission.SYSTEM_ALERT_WINDOW)) {
                builder.append("\n悬浮窗权限：\t").append(
                    if (XXPermissions.isGranted(this, Permission.SYSTEM_ALERT_WINDOW)) "已获得" else "未获得"
                )
            }
            if (permissions.contains(Permission.REQUEST_INSTALL_PACKAGES)) {
                builder.append("\n安装包权限：\t").append(
                    if (XXPermissions.isGranted(this, Permission.REQUEST_INSTALL_PACKAGES)) "已获得" else "未获得"
                )
            }
            if (permissions.contains(Manifest.permission.INTERNET)) {
                builder.append("\n当前网络访问：\t")

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        InetAddress.getByName("www.baidu.com")
                        builder.append("正常")
                    } catch (ignored: UnknownHostException) {
                        builder.append("异常")
                    }
                    lifecycleScope.launch(Dispatchers.Main) {
                        infoView?.text = builder
                    }
                }
            } else {
                infoView?.text = builder
            }
        } catch (e: PackageManager.NameNotFoundException) {
            CrashReport.postCatchedException(e)
        }
    }

    @SingleClick
    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_crash_info -> {
                drawerLayout?.openDrawer(GravityCompat.START)
            }
            R.id.iv_crash_share -> {
                // 分享文本
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, stackTrace)
                startActivity(Intent.createChooser(intent, ""))
            }
            R.id.iv_crash_restart -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        // 重启应用
        RestartActivity.restart(this)
        finish()
    }

    override fun createStatusBarConfig(): ImmersionBar {
        return super.createStatusBarConfig() // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
    }

    /**
     * 判断当前设备是否是平板
     */
    fun isTablet(): Boolean {
        return ((resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE)
    }
}