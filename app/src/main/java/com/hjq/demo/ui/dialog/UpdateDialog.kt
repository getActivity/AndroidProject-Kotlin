package com.hjq.demo.ui.dialog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.hjq.base.BaseDialog
import com.hjq.base.action.AnimAction
import com.hjq.demo.R
import com.hjq.demo.aop.CheckNet
import com.hjq.demo.aop.Permissions
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.other.AppConfig
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnDownloadListener
import com.hjq.http.model.HttpMethod
import com.hjq.permissions.Permission
import java.io.File

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/03/20
 *    desc   : 升级对话框
 */
class UpdateDialog {

    class Builder(context: Context) : BaseDialog.Builder<Builder>(context) {

        private val nameView: TextView? by lazy { findViewById(R.id.tv_update_name) }
        private val detailsView: TextView? by lazy { findViewById(R.id.tv_update_details) }
        private val progressView: ProgressBar? by lazy { findViewById(R.id.pb_update_progress) }
        private val updateView: TextView? by lazy { findViewById(R.id.tv_update_update) }
        private val closeView: TextView? by lazy { findViewById(R.id.tv_update_close) }

        /** Apk 文件 */
        private var apkFile: File? = null

        /** 下载地址 */
        private var downloadUrl: String? = null

        /** 文件 MD5 */
        private var fileMd5: String? = null

        /** 是否强制更新 */
        private var forceUpdate = false

        /** 当前是否下载中 */
        private var downloading = false

        /** 当前是否下载完毕 */
        private var downloadComplete = false

        init {
            setContentView(R.layout.update_dialog)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setCancelable(false)
            setOnClickListener(updateView, closeView)

            // 让 TextView 支持滚动
            detailsView?.movementMethod = ScrollingMovementMethod()
        }

        /**
         * 设置版本名
         */
        fun setVersionName(name: CharSequence?): Builder = apply {
            nameView?.text = name
        }

        /**
         * 设置更新日志
         */
        fun setUpdateLog(text: CharSequence?): Builder = apply {
            detailsView?.text = text
            detailsView?.visibility = if (text == null) View.GONE else View.VISIBLE
        }

        /**
         * 设置强制更新
         */
        fun setForceUpdate(force: Boolean): Builder = apply {
            forceUpdate = force
            closeView?.visibility = if (force) View.GONE else View.VISIBLE
            setCancelable(!force)
        }

        /**
         * 设置下载 url
         */
        fun setDownloadUrl(url: String?): Builder = apply {
            downloadUrl = url
        }

        /**
         * 设置文件 md5
         */
        fun setFileMd5(md5: String?): Builder = apply {
            fileMd5 = md5
        }

        @SingleClick
        override fun onClick(view: View) {
            if (view === closeView) {
                dismiss()
            } else if (view === updateView) {
                // 判断下载状态
                if (downloadComplete) {
                    if (apkFile!!.isFile) {
                        // 下载完毕，安装 Apk
                        installApk()
                    } else {
                        // 下载失败，重新下载
                        downloadApk()
                    }
                } else if (!downloading) {
                    // 没有下载，开启下载
                    downloadApk()
                }
            }
        }

        /**
         * 下载 Apk
         */
        @CheckNet
        @Permissions(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.REQUEST_INSTALL_PACKAGES)
        private fun downloadApk() {
            // 设置对话框不能被取消
            setCancelable(false)
            val notificationManager = getSystemService(NotificationManager::class.java)
            val notificationId = getContext().applicationInfo.uid
            var channelId = ""
            // 适配 Android 8.0 通知渠道新特性
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(getString(R.string.update_notification_channel_id),
                    getString(R.string.update_notification_channel_name), NotificationManager.IMPORTANCE_LOW)
                channel.enableLights(false)
                channel.enableVibration(false)
                channel.vibrationPattern = longArrayOf(0)
                channel.setSound(null, null)
                notificationManager.createNotificationChannel(channel)
                channelId = channel.id
            }
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(getContext(), channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(getString(R.string.app_name))
                // 设置通知小图标
                .setSmallIcon(R.mipmap.launcher_ic)
                // 设置通知大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.launcher_ic))
                // 设置通知静音
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                // 设置震动频率
                .setVibrate(longArrayOf(0))
                // 设置声音文件
                .setSound(null)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // 创建要下载的文件对象
            apkFile = File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                getString(R.string.app_name) + "_v" + nameView?.text.toString() + ".apk")

            EasyHttp.download(getDialog())
                .method(HttpMethod.GET)
                .file(apkFile)
                .url(downloadUrl)
                .md5(fileMd5)
                .listener(object : OnDownloadListener {
                    override fun onStart(file: File?) {
                        // 标记为下载中
                        downloading = true
                        // 标记成未下载完成
                        downloadComplete = false
                        // 后台更新
                        closeView?.visibility = View.GONE
                        // 显示进度条
                        progressView?.visibility = View.VISIBLE
                        updateView?.setText(R.string.update_status_start)
                    }

                    override fun onProgress(file: File, progress: Int) {
                        updateView?.text = String.format(getString(R.string.update_status_running)!!, progress)
                        progressView?.progress = progress
                        // 更新下载通知
                        notificationManager.notify(
                            notificationId, notificationBuilder
                                // 设置通知的文本
                                .setContentText(String.format(getString(R.string.update_status_running)!!, progress))
                                // 设置下载的进度
                                .setProgress(100, progress, false)
                                // 设置点击通知后是否自动消失
                                .setAutoCancel(false)
                                // 是否正在交互中
                                .setOngoing(true)
                                // 重新创建新的通知对象
                                .build()
                        )
                    }

                    override fun onComplete(file: File) {
                        // 显示下载成功通知
                        notificationManager.notify(
                            notificationId, notificationBuilder
                                // 设置通知的文本
                                .setContentText(String.format(getString(R.string.update_status_successful)!!, 100))
                                // 设置下载的进度
                                .setProgress(100, 100, false)
                                // 设置通知点击之后的意图
                                .setContentIntent(PendingIntent.getActivity(getContext(), 1, getInstallIntent(), Intent.FILL_IN_ACTION))
                                // 设置点击通知后是否自动消失
                                .setAutoCancel(true)
                                // 是否正在交互中
                                .setOngoing(false)
                                .build()
                        )
                        updateView?.setText(R.string.update_status_successful)
                        // 标记成下载完成
                        downloadComplete = true
                        // 安装 Apk
                        installApk()
                    }

                    override fun onError(file: File, e: Exception) {
                        // 清除通知
                        notificationManager.cancel(notificationId)
                        updateView?.setText(R.string.update_status_failed)
                        // 删除下载的文件
                        file.delete()
                    }

                    override fun onEnd(file: File) {
                        // 更新进度条
                        progressView?.progress = 0
                        progressView?.visibility = View.GONE
                        // 标记当前不是下载中
                        downloading = false
                        // 如果当前不是强制更新，对话框就恢复成可取消状态
                        if (!forceUpdate) {
                            setCancelable(true)
                        }
                    }
                }).start()
        }

        /**
         * 安装 Apk
         */
        @Permissions(Permission.REQUEST_INSTALL_PACKAGES)
        private fun installApk() {
            getContext().startActivity(getInstallIntent())
        }

        /**
         * 获取安装意图
         */
        private fun getInstallIntent(): Intent {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val uri: Uri?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(getContext(), AppConfig.getPackageName() + ".provider", apkFile!!)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                uri = Uri.fromFile(apkFile)
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }
}