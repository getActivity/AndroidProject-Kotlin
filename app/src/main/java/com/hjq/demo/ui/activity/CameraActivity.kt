package com.hjq.demo.ui.activity

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.hjq.base.BaseActivity
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.Permissions
import com.hjq.demo.app.AppActivity
import com.hjq.demo.other.AppConfig
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/18
 *    desc   : 拍摄图片、视频
 */
class CameraActivity : AppActivity() {

    companion object {

        const val INTENT_KEY_IN_FILE: String = "file"
        const val INTENT_KEY_IN_VIDEO: String = "video"
        const val INTENT_KEY_OUT_ERROR: String = "error"

        fun start(activity: BaseActivity, listener: OnCameraListener?) {
            start(activity, false, listener)
        }

        @Log
        @Permissions(Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE, Permission.CAMERA)
        fun start(activity: BaseActivity, video: Boolean, listener: OnCameraListener?) {
            val file: File = createCameraFile(video)
            val intent = Intent(activity, CameraActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_FILE, file)
            intent.putExtra(INTENT_KEY_IN_VIDEO, video)
            activity.startActivityForResult(intent, object : OnActivityCallback {

                override fun onActivityResult(resultCode: Int, data: Intent?) {
                    if (listener == null) {
                        return
                    }
                    when (resultCode) {
                        RESULT_OK -> {
                            if (file.isFile) {
                                listener.onSelected(file)
                            } else {
                                listener.onCancel()
                            }
                        }
                        RESULT_ERROR -> {
                            var details: String? = null
                            if (data != null) {
                                details = data.getStringExtra(INTENT_KEY_OUT_ERROR)
                            }
                            if (details == null) {
                                details = activity.getString(R.string.common_unknown_error)
                            }
                            listener.onError(details)
                        }
                        RESULT_CANCELED -> listener.onCancel()
                        else -> listener.onCancel()
                    }
                }
            })
        }

        /**
         * 创建一个拍照图片文件路径
         */
        @Suppress("deprecation")
        private fun createCameraFile(video: Boolean): File {
            var folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera")
            if (!folder.exists() || !folder.isDirectory) {
                if (!folder.mkdirs()) {
                    folder = Environment.getExternalStorageDirectory()
                }
            }
            return File(folder, ((if (video) "VID" else "IMG") + "_" +
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) +
                        (if (video) ".mp4" else ".jpg")))
        }
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun initView() {}

    override fun initData() {
        val intent = Intent()
        // 启动系统相机
        if (getBoolean(INTENT_KEY_IN_VIDEO)) {
            // 录制视频
            intent.action = MediaStore.ACTION_VIDEO_CAPTURE
        } else {
            // 拍摄照片
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        }
        if (intent.resolveActivity(packageManager) == null || !XXPermissions.isGranted(this,
                Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA)) {
            setResult(RESULT_ERROR, Intent().putExtra(INTENT_KEY_OUT_ERROR, getString(R.string.camera_launch_fail)))
            finish()
            return
        }
        val file: File? = getSerializable(INTENT_KEY_IN_FILE)
        if (file == null) {
            setResult(RESULT_ERROR, Intent().putExtra(INTENT_KEY_OUT_ERROR, getString(R.string.camera_image_error)))
            finish()
            return
        }
        val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通过 FileProvider 创建一个 Content 类型的 Uri 文件
            FileProvider.getUriForFile(this, AppConfig.getPackageName() + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
        // 对目标应用临时授权该 Uri 所代表的文件
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        // 将拍取的照片保存到指定 Uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, object : OnActivityCallback {
            override fun onActivityResult(resultCode: Int, data: Intent?) {
                if (resultCode == RESULT_OK) {
                    // 通知系统多媒体扫描该文件，否则会导致拍摄出来的图片或者视频没有及时显示到相册中，而需要通过重启手机才能看到
                    MediaScannerConnection.scanFile(applicationContext, arrayOf(file.path), null, null)
                }
                setResult(resultCode)
                finish()
            }
        })
    }

    /**
     * 拍照选择监听
     */
    interface OnCameraListener {

        /**
         * 选择回调
         *
         * @param file          文件
         */
        fun onSelected(file: File)

        /**
         * 错误回调
         *
         * @param details       错误详情
         */
        fun onError(details: String)

        /**
         * 取消回调
         */
        fun onCancel() {}
    }
}