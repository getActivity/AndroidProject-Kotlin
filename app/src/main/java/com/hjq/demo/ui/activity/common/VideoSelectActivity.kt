package com.hjq.demo.ui.activity.common

import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hjq.bar.TitleBar
import com.hjq.base.BaseActivity
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.base.ktx.startActivityForResult
import com.hjq.custom.widget.view.FloatActionButton
import com.hjq.demo.R
import com.hjq.demo.action.StatusAction
import com.hjq.demo.aop.Log
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.ktx.toast
import com.hjq.demo.other.GridSpaceDecoration
import com.hjq.demo.permission.PermissionDescription
import com.hjq.demo.permission.PermissionInterceptor
import com.hjq.demo.ui.activity.common.CameraActivity.OnCameraListener
import com.hjq.demo.ui.adapter.common.VideoSelectAdapter
import com.hjq.demo.ui.dialog.common.AlbumDialog
import com.hjq.demo.ui.dialog.common.AlbumDialog.AlbumInfo
import com.hjq.demo.widget.StatusLayout
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.smallest.width.dp2px
import com.tencent.bugly.library.Bugly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Objects

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/03/01
 *    desc   : 选择视频
 */
class VideoSelectActivity : AppActivity(), StatusAction, Runnable, BaseAdapter.OnItemClickListener,
    BaseAdapter.OnItemLongClickListener, BaseAdapter.OnChildClickListener {

    companion object {

        private const val INTENT_KEY_IN_MAX_SELECT: String = "maxSelect"
        private const val INTENT_KEY_OUT_VIDEO_LIST: String = "videoList"

        fun start(activity: BaseActivity, listener: OnVideoSelectListener?) {
            start(activity, 1, listener)
        }

        @Log
        fun start(activity: BaseActivity, maxSelect: Int, listener: OnVideoSelectListener?) {
            if (maxSelect < 1) {
                // 最少要选择一个视频
                throw IllegalArgumentException("are you ok?")
            }

            if (PickVisualMedia.isPhotoPickerAvailable(activity)) {
                val visualMediaRequest = PickVisualMediaRequest.Builder()
                    // 只选择视频
                    .setMediaType(PickVisualMedia.VideoOnly)
                    .build()

                if (maxSelect > 1) {
                    val multipleVisualMedia = PickMultipleVisualMedia(maxSelect)
                    val intent = multipleVisualMedia.createIntent(activity, visualMediaRequest)
                    activity.startActivityForResult(intent, null, OnActivityCallback { resultCode, data ->
                        val uris: MutableList<Uri> = multipleVisualMedia.parseResult(resultCode, data).toMutableList()
                        if (uris.isEmpty()) {
                            return@OnActivityCallback
                        }
                        val list: MutableList<String> = mutableListOf()
                        for (i in uris.indices) {
                            list.add(uris[i].toString())
                        }
                        listener?.onSelected(list)
                    })
                } else {
                    val pickVisualMedia = PickVisualMedia()
                    val intent = pickVisualMedia.createIntent(activity, visualMediaRequest)
                    activity.startActivityForResult(intent, null, OnActivityCallback { resultCode, data ->
                        val uri = pickVisualMedia.parseResult(resultCode, data) ?: return@OnActivityCallback
                        val list: MutableList<String> = mutableListOf()
                        list.add(uri.toString())
                        listener?.onSelected(list)
                    })
                }
                return
            }

            XXPermissions.with(activity)
                .permission(PermissionLists.getReadExternalStoragePermission())
                .interceptor(PermissionInterceptor())
                .description(PermissionDescription())
                // 设置不触发错误检测机制
                .unchecked()
                .request { _, deniedList ->
                    val allGranted = deniedList.isEmpty()
                    if (!allGranted) {
                        return@request
                    }

                    activity.startActivityForResult(VideoSelectActivity::class.java, {
                        putExtra(INTENT_KEY_IN_MAX_SELECT, maxSelect)
                    }, OnActivityCallback { resultCode, data ->
                        if (data == null) {
                            listener?.onCancel()
                            return@OnActivityCallback
                        }
                        val videoBeans: MutableList<VideoBean>? = data.getParcelableArrayListExtra(INTENT_KEY_OUT_VIDEO_LIST)
                        if (videoBeans.isNullOrEmpty()) {
                            listener?.onCancel()
                            return@OnActivityCallback
                        }
                        val iterator: MutableIterator<VideoBean> = videoBeans.iterator()
                        while (iterator.hasNext()) {
                            if (!File(iterator.next().getVideoPath()).isFile) {
                                iterator.remove()
                            }
                        }

                        val list: MutableList<String> = mutableListOf()
                        for (videoBean in videoBeans) {
                            list.add(videoBean.getVideoPath())
                        }
                        if (resultCode == RESULT_OK && list.isNotEmpty()) {
                            listener?.onSelected(list)
                            return@OnActivityCallback
                        }
                        listener?.onCancel()
                    })
                }
        }
    }

    private val statusLayout: StatusLayout? by lazyFindViewById(R.id.sl_video_select_status)
    private val recyclerView: RecyclerView? by lazyFindViewById(R.id.rv_video_select_list)
    private val floatingView: FloatActionButton? by lazyFindViewById(R.id.fab_video_select_floating)

    /** 最大选中 */
    private var maxSelect: Int = 1

    /** 选中列表 */
    private val selectVideo: MutableList<VideoBean> = mutableListOf()

    /** 全部视频 */
    private val allVideo: MutableList<VideoBean> = mutableListOf()

    /** 视频专辑 */
    private val allAlbum: MutableMap<String, MutableList<VideoBean>> = mutableMapOf()

    /** 列表适配器 */
    private val adapter: VideoSelectAdapter = VideoSelectAdapter(this, selectVideo)

    /** 专辑选择对话框 */
    private var albumDialog: AlbumDialog.Builder? = null

    override fun getLayoutId(): Int {
        return R.layout.video_select_activity
    }

    override fun initView() {
        setOnClickListener(floatingView)
        adapter.setOnChildClickListener(R.id.fl_video_select_check, this)
        adapter.setOnItemClickListener(this)
        adapter.setOnItemLongClickListener(this)

        recyclerView?.let {
            it.adapter = adapter
            // 禁用动画效果
            it.itemAnimator = null
            // 添加分割线
            it.addItemDecoration(GridSpaceDecoration(dp2px(5).toInt()))
            // 设置滚动监听
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> floatingView?.hide()
                        RecyclerView.SCROLL_STATE_IDLE -> floatingView?.show()
                    }
                }
            })
        }
    }

    override fun initData() {
        // 获取最大的选择数
        maxSelect = getInt(INTENT_KEY_IN_MAX_SELECT, maxSelect)

        // 显示加载进度条
        showLoading()
        // 加载视频列表
        lifecycleScope.launch(Dispatchers.IO) { run() }
    }

    override fun acquireStatusLayout(): StatusLayout? {
        return statusLayout
    }

    @SingleClick
    override fun onRightClick(titleBar: TitleBar) {
        if (allVideo.isEmpty()) {
            return
        }
        val data: MutableList<AlbumInfo> = mutableListOf()
        var count = 0
        val keys: MutableSet<String> = allAlbum.keys
        for (key: String in keys) {
            val list: MutableList<VideoBean>? = allAlbum[key]
            if (list == null || list.isEmpty()) {
                continue
            }
            count += list.size
            data.add(AlbumInfo(list[0].getVideoPath(), key, String.format(getString(R.string.video_select_total), list.size),
                adapter.getData() !== allVideo && adapter.getData() === list))
        }
        data.add(0, AlbumInfo(allVideo[0].getVideoPath(), getString(R.string.video_select_all),
            String.format(getString(R.string.video_select_total), count), adapter.getData() === allVideo))
        if (albumDialog == null) {
            albumDialog = AlbumDialog.Builder(this)
                .setListener(object : AlbumDialog.OnListener {

                    override fun onSelected(dialog: BaseDialog, position: Int, bean: AlbumInfo) {
                        setRightTitle(bean.getName())
                        // 滚动回第一个位置
                        recyclerView?.scrollToPosition(0)
                        if (position == 0) {
                            adapter.setData(allVideo)
                        } else {
                            adapter.setData(allAlbum[bean.getName()])
                        }
                        // 执行列表动画
                        recyclerView?.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                            this@VideoSelectActivity, R.anim.layout_from_right)
                        recyclerView?.scheduleLayoutAnimation()
                    }
                })
        }
        albumDialog?.setData(data)
            ?.show()
    }

    override fun onRestart() {
        super.onRestart()
        val iterator: MutableIterator<VideoBean> = selectVideo.iterator()
        // 遍历判断选择了的视频是否被删除了
        while (iterator.hasNext()) {
            val bean: VideoBean = iterator.next()
            val file = File(bean.getVideoPath())
            if (file.isFile) {
                continue
            }
            iterator.remove()
            allVideo.remove(bean)
            val parentFile: File = file.parentFile ?: continue
            val data = allAlbum[parentFile.name]
            data?.remove(bean)
            if (data === adapter.getData()) {
                adapter.removeItem(bean)
            }
            if (selectVideo.isEmpty()) {
                floatingView?.setImageResource(R.drawable.videocam_ic)
            } else {
                floatingView?.setImageResource(R.drawable.succeed_ic)
            }
        }
        refreshLayout()
    }

    @SingleClick
    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab_video_select_floating -> {
                if (selectVideo.isEmpty()) {
                    // 点击拍照
                    CameraActivity.start(this, true, object : OnCameraListener {

                        override fun onSelected(file: File) {
                            // 当前选中视频的数量必须小于最大选中数
                            if (selectVideo.size < maxSelect) {
                                selectVideo.add(VideoBean.newInstance(file.path))
                            }

                            // 这里需要延迟刷新，否则可能会找不到拍照的视频
                            postDelayed({
                                // 重新加载视频列表
                                lifecycleScope.launch { run() }
                            }, 1000)
                        }

                        override fun onError(details: String) {
                            toast(details)
                        }
                    })
                    return
                }

                // 完成选择
                setResult(RESULT_OK, Intent().putParcelableArrayListExtra(INTENT_KEY_OUT_VIDEO_LIST,
                          selectVideo.toCollection(ArrayList())))
                finish()
            }
        }
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     * @param recyclerView      RecyclerView对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    override fun onItemClick(recyclerView: RecyclerView, itemView: View, position: Int) {
        adapter.getItem(position).apply {
            VideoPlayActivity.Builder()
                .setVideoSource(File(getVideoPath()))
                .setActivityOrientation(if (getVideoWidth() > getVideoHeight()) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .start(this@VideoSelectActivity)
        }
    }

    /**
     * [BaseAdapter.OnItemLongClickListener]
     * @param recyclerView      RecyclerView对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    override fun onItemLongClick(recyclerView: RecyclerView, itemView: View, position: Int): Boolean {
        if (selectVideo.size < maxSelect) {
            // 长按的时候模拟选中
            itemView?.findViewById<View?>(R.id.fl_video_select_check)?.let {
                return it.performClick()
            }
        }
        return false
    }

    /**
     * [BaseAdapter.OnChildClickListener]
     * @param recyclerView      RecyclerView对象
     * @param childView         被点击的条目子 View Id
     * @param position          被点击的条目位置
     */
    override fun onChildClick(recyclerView: RecyclerView, childView: View, position: Int) {
        when (childView?.id) {
            R.id.fl_video_select_check -> {
                val bean: VideoBean = adapter.getItem(position)
                val file = File(bean.getVideoPath())
                if (!file.isFile) {
                    adapter.removeItem(position)
                    toast(R.string.video_select_error)
                    return
                }
                if (selectVideo.contains(bean)) {
                    selectVideo.remove(bean)
                    if (selectVideo.isEmpty()) {
                        floatingView?.setImageResource(R.drawable.videocam_ic)
                    }
                    adapter.notifyItemChanged(position)
                    return
                }
                if (maxSelect == 1 && selectVideo.size == 1) {
                    val data: MutableList<VideoBean> = adapter.getData()
                    val index: Int = data.indexOf(selectVideo.removeAt(0))
                    if (index != -1) {
                        adapter.notifyItemChanged(index)
                    }
                    selectVideo.add(bean)
                } else if (selectVideo.size < maxSelect) {
                    selectVideo.add(bean)
                    if (selectVideo.size == 1) {
                        floatingView?.setImageResource(R.drawable.succeed_ic)
                    }
                } else {
                    toast(String.format(getString(R.string.video_select_max_hint), maxSelect))
                }
                adapter.notifyItemChanged(position)
            }
        }
    }

    override fun run() {
        allAlbum.clear()
        allVideo.clear()
        val contentUri: Uri = MediaStore.Files.getContentUri("external")
        val sortOrder: String = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        val selection: String = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" + " AND " + MediaStore.MediaColumns.SIZE + ">0"
        val contentResolver: ContentResolver = contentResolver
        val projections: Array<String?> = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.SIZE, MediaStore.Video.Media.DURATION)
        var cursor: Cursor? = null
        if (XXPermissions.isGrantedPermissions(this, mutableListOf(PermissionLists.getReadExternalStoragePermission(), PermissionLists.getWriteExternalStoragePermission()))) {
            cursor = contentResolver.query(contentUri, projections, selection,
                arrayOf<String?>(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()), sortOrder)
        }
        if (cursor != null && cursor.moveToFirst()) {
            val pathIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            val mimeTypeIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
            val sizeIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            val durationIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)
            val widthIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
            val heightIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
            do {
                val duration: Long = cursor.getLong(durationIndex)
                // 视频时长不得小于 1 秒
                if (duration < 1000) {
                    continue
                }
                val size: Long = cursor.getLong(sizeIndex)
                // 视频大小不得小于 10 KB
                if (size < 1024 * 10) {
                    continue
                }
                val type: String? = cursor.getString(mimeTypeIndex)
                if (type == null || type.isEmpty()) {
                    continue
                }
                val path: String? = cursor.getString(pathIndex)
                if (path == null || path.isEmpty()) {
                    continue
                }
                val file = File(path)
                if (!file.exists() || !file.isFile) {
                    continue
                }
                val parentFile: File = file.parentFile ?: continue

                // 获取目录名作为专辑名称
                val albumName: String = parentFile.name
                var data: MutableList<VideoBean>? = allAlbum[albumName]
                if (data == null) {
                    data = mutableListOf()
                    allAlbum[albumName] = data
                }
                val width: Int = cursor.getInt(widthIndex)
                val height: Int = cursor.getInt(heightIndex)
                val bean = VideoBean(path, width, height, duration, size)
                data.add(bean)
                allVideo.add(bean)
            } while (cursor.moveToNext())
            cursor.close()
        }

        post {
            // 滚动回第一个位置
            recyclerView?.scrollToPosition(0)
            // 设置新的列表数据
            adapter.setData(allVideo)
            if (selectVideo.isEmpty()) {
                floatingView?.setImageResource(R.drawable.videocam_ic)
            } else {
                floatingView?.setImageResource(R.drawable.succeed_ic)
            }

            // 执行列表动画
            recyclerView?.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                this@VideoSelectActivity, R.anim.layout_fall_down
            )
            recyclerView?.scheduleLayoutAnimation()

            refreshLayout()
        }
    }

    /**
     * 刷新布局
     */
    private fun refreshLayout() {
        if (allVideo.isEmpty()) {
            // 显示空布局
            showEmpty()
            // 设置右标题
            setRightTitle(null)
        } else {
            // 显示加载完成
            showComplete()
            // 设置右标题
            setRightTitle(R.string.video_select_all)
        }
    }

    /**
     * 视频 Bean 类
     */
    class VideoBean : Parcelable {

        private val videoPath: String
        private val videoWidth: Int
        private val videoHeight: Int
        private val videoDuration: Long
        private val videoSize: Long

        constructor(path: String, width: Int, height: Int, duration: Long, size: Long) {
            videoPath = path
            videoWidth = width
            videoHeight = height
            videoDuration = duration
            videoSize = size
        }

        fun getVideoWidth(): Int {
            return videoWidth
        }

        fun getVideoHeight(): Int {
            return videoHeight
        }

        fun getVideoPath(): String {
            return videoPath
        }

        fun getVideoDuration(): Long {
            return videoDuration
        }

        fun getVideoSize(): Long {
            return videoSize
        }

        override fun equals(obj: Any?): Boolean {
            if (obj is VideoBean) {
                return (videoPath == obj.videoPath)
            }
            return false
        }

        override fun hashCode(): Int {
            return Objects.hash(videoPath, videoWidth, videoHeight, videoDuration, videoSize)
        }

        override fun toString(): String {
            return videoPath
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(videoPath)
            dest.writeInt(videoWidth)
            dest.writeInt(videoHeight)
            dest.writeLong(videoDuration)
            dest.writeLong(videoSize)
        }

        constructor(`in`: Parcel) {
            videoPath = `in`.readString() ?: ""
            videoWidth = `in`.readInt()
            videoHeight = `in`.readInt()
            videoDuration = `in`.readLong()
            videoSize = `in`.readLong()
        }

        companion object {

            fun newInstance(videoPath: String): VideoBean {
                var videoWidth = 0
                var videoHeight = 0
                var videoDuration: Long = 0
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(videoPath)
                    val widthMetadata: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    if (widthMetadata != null && "" != widthMetadata) {
                        videoWidth = widthMetadata.toInt()
                    }
                    val heightMetadata: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    if (heightMetadata != null && "" != heightMetadata) {
                        videoHeight = heightMetadata.toInt()
                    }
                    val durationMetadata: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    if (durationMetadata != null && "" != durationMetadata) {
                        videoDuration = durationMetadata.toLong()
                    }
                } catch (e: RuntimeException) {
                    // 荣耀 LLD AL20 Android 8.0 出现：java.lang.IllegalArgumentException
                    // 荣耀 HLK AL00 Android 10.0 出现：java.lang.RuntimeException：setDataSource failed: status = 0x80000000
                    e.printStackTrace()
                    Bugly.handleCatchException(Thread.currentThread(), e, e.message, null, true)
                }
                val videoSize: Long = File(videoPath).length()
                return VideoBean(videoPath, videoWidth, videoHeight, videoDuration, videoSize)
            }

            @JvmField
            val CREATOR: Parcelable.Creator<VideoBean?> = object : Parcelable.Creator<VideoBean?> {
                override fun createFromParcel(source: Parcel): VideoBean {
                    return VideoBean(source)
                }

                override fun newArray(size: Int): Array<VideoBean?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    /**
     * 视频选择监听
     */
    interface OnVideoSelectListener {

        /**
         * 选择回调
         *
         * @param data          视频列表
         */
        fun onSelected(data: MutableList<String>)

        /**
         * 取消回调
         */
        fun onCancel() {
            // default implementation ignored
        }
    }
}