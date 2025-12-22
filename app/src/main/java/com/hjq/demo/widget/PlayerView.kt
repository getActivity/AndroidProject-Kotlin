package com.hjq.demo.widget

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.VideoView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.core.action.ContextAction
import com.hjq.core.ktx.getActivity
import com.hjq.custom.widget.layout.SimpleLayout
import com.hjq.custom.widget.view.PlayButton
import com.hjq.demo.R
import com.hjq.demo.ui.dialog.common.MessageDialog
import java.io.File
import java.util.Formatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/02/16
 *    desc   : 视频播放控件
 */
class PlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    SimpleLayout(context, attrs, defStyleAttr, defStyleRes), LifecycleEventObserver,
    OnSeekBarChangeListener, View.OnClickListener, ContextAction, OnPreparedListener,
    MediaPlayer.OnInfoListener, OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {

        /** 刷新间隔 */
        private const val REFRESH_TIME: Int = 1000

        /** 面板隐藏间隔 */
        private const val CONTROLLER_TIME: Int = 3000

        /** 提示对话框隐藏间隔 */
        private const val DIALOG_TIME: Int = 500

        /** 动画执行时间 */
        private const val ANIM_TIME: Int = 500

        /**
         * 时间转换
         */
        fun conversionTime(time: Int): String {
            val formatter = Formatter(Locale.getDefault())
            // 总秒数
            val totalSeconds: Int = time / 1000
            // 小时数
            val hours: Int = totalSeconds / 3600
            // 分钟数
            val minutes: Int = (totalSeconds / 60) % 60
            // 秒数
            val seconds: Int = totalSeconds % 60
            return if (hours > 0) {
                formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
            } else {
                formatter.format("%02d:%02d", minutes, seconds).toString()
            }
        }
    }

    private val topLayout: ViewGroup by lazyFindViewById(R.id.ll_player_view_top)
    private val titleView: TextView by lazyFindViewById(R.id.tv_player_view_title)
    private val backView: ImageView by lazyFindViewById(R.id.iv_player_view_left)
    private val bottomLayout: ViewGroup by lazyFindViewById(R.id.ll_player_view_bottom)
    private val playTime: TextView by lazyFindViewById(R.id.tv_player_view_play_time)
    private val totalTime: TextView by lazyFindViewById(R.id.tv_player_view_total_time)
    private val progressView: SeekBar by lazyFindViewById(R.id.sb_player_view_progress)
    private val videoView: VideoView by lazyFindViewById(R.id.vv_player_view_video)
    private val controlView: PlayButton by lazyFindViewById(R.id.iv_player_view_control)
    private val lockView: ImageView by lazyFindViewById(R.id.iv_player_view_lock)
    private val messageLayout: ViewGroup by lazyFindViewById(R.id.cv_player_view_message)
    private val lottieView: LottieAnimationView by lazyFindViewById(R.id.lav_player_view_lottie)
    private val messageView: TextView by lazyFindViewById(R.id.tv_player_view_message)

    /** 视频宽度 */
    private var videoWidth: Int = 0

    /** 视频高度 */
    private var videoHeight: Int = 0

    /** 锁定面板 */
    private var lockMode: Boolean = false

    /** 显示面板 */
    private var controllerShow: Boolean = false

    /** 触摸按下的 X 坐标 */
    private var viewDownX: Float = 0f

    /** 触摸按下的 Y 坐标 */
    private var viewDownY: Float = 0f

    /** 手势开关 */
    private var gestureEnabled: Boolean = false

    /** 当前播放进度 */
    private var currentProgress: Int = 0

    /** 返回监听器 */
    private var listener: OnPlayListener? = null

    /** 音量管理器 */
    private val audioManager: AudioManager?

    /** 最大音量值 */
    private var maxVoice: Int = 0

    /** 当前音量值 */
    private var currentVolume: Int = 0

    /** 当前亮度值百分比 */
    private var currentBrightnessPercent: Float = 0f

    /** 当前窗口对象 */
    private var window: Window? = null

    /** 调整秒数 */
    private var adjustSecond: Int = 0

    /** 触摸方向 */
    private var touchOrientation: Int = -1

    /**
     * 刷新任务
     */
    private val refreshRunnable: Runnable by lazy { object : Runnable {

        override fun run() {
            var progress: Int = videoView.currentPosition
            // 这里优化了播放的秒数计算，将 800 毫秒估算成 1 秒
            if (progress + 1000 < videoView.duration) {
                // 进行四舍五入计算
                progress = (progress / 1000f).roundToInt() * 1000
            }
            playTime.text = conversionTime(progress)
            progressView.progress = progress
            progressView.secondaryProgress = (videoView.bufferPercentage / 100f * videoView.duration).toInt()
            if (videoView.isPlaying) {
                if (!lockMode && bottomLayout.visibility == GONE) {
                    bottomLayout.visibility = VISIBLE
                }
                if (!videoView.getKeepScreenOn()) {
                    videoView.setKeepScreenOn(true)
                }
            } else {
                if (bottomLayout.visibility == VISIBLE) {
                    bottomLayout.visibility = GONE
                }
                if (videoView.getKeepScreenOn()) {
                    videoView.setKeepScreenOn(false)
                }
            }
            postDelayed(this, REFRESH_TIME.toLong())
            listener?.onPlayProgress(this@PlayerView)
        }
    }}

    /**
     * 显示控制面板
     */
    private val showControllerRunnable: Runnable by lazy { Runnable {
        if (!controllerShow) {
            showController()
        }
    }}

    /**
     * 隐藏控制面板
     */
    private val hideControllerRunnable: Runnable by lazy { Runnable {
        if (controllerShow) {
            hideController()
        }
    }}

    /**
     * 显示提示
     */
    private val showMessageRunnable: Runnable by lazy { Runnable {
        hideController()
        messageLayout.visibility = VISIBLE
    }}

    /**
     * 隐藏提示
     */
    private val hideMessageRunnable: Runnable by lazy { Runnable { messageLayout.visibility = GONE }}

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_player_view, this, true)
        backView.setOnClickListener(this)
        controlView.setOnClickListener(this)
        lockView.setOnClickListener(this)
        setOnClickListener(this)
        progressView.setOnSeekBarChangeListener(this)
        videoView.setOnPreparedListener(this)
        videoView.setOnCompletionListener(this)
        videoView.setOnInfoListener(this)
        videoView.setOnErrorListener(this)
        audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)

        // 注意这里不要用 View 的 getLayoutDirection() 来判断，因为获取到的不准确
        val backIconDrawable: Drawable? = if (resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
            ContextCompat.getDrawable(context, R.drawable.arrows_right_ic)
        } else {
            ContextCompat.getDrawable(context, R.drawable.arrows_left_ic)
        }
        backView.setImageDrawable(backIconDrawable)

        val activity = context.getActivity()
        if (activity != null) {
            window = activity.window
        }
    }

    /**
     * 设置播放器生命管控（自动回调生命周期方法）
     */
    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    /**
     * [LifecycleEventObserver]
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> {
                // default implementation ignored
            }
        }
    }

    /**
     * 设置视频标题
     */
    fun setVideoTitle(title: CharSequence?) {
        if (TextUtils.isEmpty(title)) {
            return
        }
        titleView.text = title
    }

    /**
     * 设置视频源
     */
    fun setVideoSource(file: File?) {
        if (file == null || !file.isFile) {
            return
        }
        videoView.setVideoPath(file.path)
    }

    fun setVideoSource(url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        videoView.setVideoURI(Uri.parse(url))
    }

    /**
     * 开始播放
     */
    fun start() {
        videoView.start()
        controlView.play()
        // 延迟隐藏控制面板
        removeCallbacks(hideControllerRunnable)
        postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
    }

    /**
     * 暂停播放
     */
    fun pause() {
        videoView.pause()
        controlView.pause()
        // 延迟隐藏控制面板
        removeCallbacks(hideControllerRunnable)
        postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
    }

    /**
     * 锁定控制面板
     */
    fun lock() {
        lockMode = true
        lockView.setImageResource(R.drawable.video_lock_close_ic)
        topLayout.visibility = GONE
        bottomLayout.visibility = GONE
        controlView.visibility = GONE
        // 延迟隐藏控制面板
        removeCallbacks(hideControllerRunnable)
        postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
    }

    /**
     * 解锁控制面板
     */
    fun unlock() {
        lockMode = false
        lockView.setImageResource(R.drawable.video_lock_open_ic)
        topLayout.visibility = VISIBLE
        if (videoView.isPlaying) {
            bottomLayout.visibility = VISIBLE
        }
        controlView.visibility = VISIBLE
        // 延迟隐藏控制面板
        removeCallbacks(hideControllerRunnable)
        postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
    }

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean {
        return videoView.isPlaying
    }

    /**
     * 设置视频播放进度
     */
    fun setProgress(progress: Int) {
        var actualProgress: Int = progress
        if (actualProgress > videoView.duration) {
            actualProgress = videoView.duration
        }
        // 要跳转的进度必须和当前播放进度相差 1 秒以上
        if (abs(actualProgress - videoView.currentPosition) > 1000) {
            videoView.seekTo(actualProgress)
            progressView.progress = actualProgress
        }
    }

    /**
     * 获取视频播放进度
     */
    fun getProgress(): Int {
        return videoView.currentPosition
    }

    /**
     * 获取视频的总进度
     */
    fun getDuration(): Int {
        return videoView.duration
    }

    /**
     * 设置手势开关
     */
    fun setGestureEnabled(enabled: Boolean) {
        gestureEnabled = enabled
    }

    /**
     * 设置返回监听
     */
    fun setOnPlayListener(listener: OnPlayListener?) {
        this.listener = listener
        backView.visibility = if (this.listener != null) VISIBLE else INVISIBLE
    }

    /**
     * 显示面板
     */
    fun showController() {
        if (controllerShow) {
            return
        }
        controllerShow = true
        val topAnimator: ValueAnimator = ValueAnimator.ofInt(-topLayout.height, 0)
        topAnimator.duration = ANIM_TIME.toLong()
        topAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val translationY: Int = animation.animatedValue as Int
            topLayout.translationY = translationY.toFloat()
            if (translationY != -topLayout.height) {
                return@AnimatorUpdateListener
            }
            if (topLayout.visibility == INVISIBLE) {
                topLayout.visibility = VISIBLE
            }
        })
        topAnimator.start()
        val bottomAnimator: ValueAnimator = ValueAnimator.ofInt(bottomLayout.height, 0)
        bottomAnimator.duration = ANIM_TIME.toLong()
        bottomAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val translationY: Int = animation.animatedValue as Int
            bottomLayout.translationY = translationY.toFloat()
            if (translationY != bottomLayout.height) {
                return@AnimatorUpdateListener
            }
            if (bottomLayout.visibility == INVISIBLE) {
                bottomLayout.visibility = VISIBLE
            }
        })
        bottomAnimator.start()
        val alphaAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        alphaAnimator.duration = ANIM_TIME.toLong()
        alphaAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val alpha: Float = animation.animatedValue as Float
            lockView.alpha = alpha
            controlView.alpha = alpha
            if (alpha != 0f) {
                return@AnimatorUpdateListener
            }
            if (lockView.visibility == INVISIBLE) {
                lockView.visibility = VISIBLE
            }
            if (controlView.visibility == INVISIBLE) {
                controlView.visibility = VISIBLE
            }
        })
        alphaAnimator.start()
    }

    /**
     * 隐藏面板
     */
    fun hideController() {
        if (!controllerShow) {
            return
        }
        controllerShow = false
        val topAnimator: ValueAnimator = ValueAnimator.ofInt(0, -topLayout.height)
        topAnimator.duration = ANIM_TIME.toLong()
        topAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val translationY: Int = animation.animatedValue as Int
            topLayout.translationY = translationY.toFloat()
            if (translationY != -topLayout.height) {
                return@AnimatorUpdateListener
            }
            if (topLayout.visibility == VISIBLE) {
                topLayout.visibility = INVISIBLE
            }
        })
        topAnimator.start()
        val bottomAnimator: ValueAnimator = ValueAnimator.ofInt(0, bottomLayout.height)
        bottomAnimator.duration = ANIM_TIME.toLong()
        bottomAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val translationY: Int = animation.animatedValue as Int
            bottomLayout.translationY = translationY.toFloat()
            if (translationY != bottomLayout.height) {
                return@AnimatorUpdateListener
            }
            if (bottomLayout.visibility == VISIBLE) {
                bottomLayout.visibility = INVISIBLE
            }
        })
        bottomAnimator.start()
        val alphaAnimator: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)
        alphaAnimator.duration = ANIM_TIME.toLong()
        alphaAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val alpha: Float = animation.animatedValue as Float
            lockView.alpha = alpha
            controlView.alpha = alpha
            if (alpha != 0f) {
                return@AnimatorUpdateListener
            }
            if (lockView.visibility == VISIBLE) {
                lockView.visibility = INVISIBLE
            }
            if (controlView.visibility == VISIBLE) {
                controlView.visibility = INVISIBLE
            }
        })
        alphaAnimator.start()
    }

    fun onResume() {
        videoView.resume()
    }

    fun onPause() {
        videoView.suspend()
        pause()
    }

    fun onDestroy() {
        videoView.stopPlayback()
        removeCallbacks(refreshRunnable)
        removeCallbacks(showControllerRunnable)
        removeCallbacks(hideControllerRunnable)
        removeCallbacks(showMessageRunnable)
        removeCallbacks(hideMessageRunnable)
        removeAllViews()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        // 这里解释一下 onWindowVisibilityChanged 方法调用的时机
        // 从前台返回到后台：先调用 onWindowVisibilityChanged(View.INVISIBLE) 后调用 onWindowVisibilityChanged(View.GONE)
        // 从后台返回到前台：先调用 onWindowVisibilityChanged(View.INVISIBLE) 后调用 onWindowVisibilityChanged(View.VISIBLE)
        super.onWindowVisibilityChanged(visibility)
        // 这里修复了 Activity 从后台返回到前台时 VideoView 从头开始播放的问题
        if (visibility != VISIBLE) {
            return
        }
        videoView.seekTo(currentProgress)
        progressView.progress = currentProgress
    }

    /**
     * [SeekBar.OnSeekBarChangeListener]
     */
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            playTime.text = conversionTime(progress)
            return
        }
        if (progress != 0) {
            // 记录当前播放进度
            currentProgress = progress
        } else {
            // 如果 Activity 返回到后台，progress 会等于 0，而 mVideoView.getDuration 会等于 -1
            // 所以要避免在这种情况下记录当前的播放进度，以便用户从后台返回到前台的时候恢复正确的播放进度
            if (videoView.duration > 0) {
                currentProgress = progress
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        removeCallbacks(refreshRunnable)
        removeCallbacks(hideControllerRunnable)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        postDelayed(refreshRunnable, REFRESH_TIME.toLong())
        postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
        // 设置选择的播放进度
        setProgress(seekBar.progress)
    }

    /**
     * [MediaPlayer.OnPreparedListener]
     */
    override fun onPrepared(player: MediaPlayer) {
        playTime.text = conversionTime(0)
        totalTime.text = conversionTime(player.duration)
        progressView.max = videoView.duration

        // 获取视频的宽高
        videoWidth = player.videoWidth
        videoHeight = player.videoHeight

        // VideoView 的宽高
        var viewWidth: Int = width
        var viewHeight: Int = height

        // 基于比例调整大小
        if (videoWidth * viewHeight < viewWidth * videoHeight) {
            // 视频宽度过大，进行纠正
            viewWidth = viewHeight * videoWidth / videoHeight
        } else if (videoWidth * viewHeight > viewWidth * videoHeight) {
            // 视频高度过大，进行纠正
            viewHeight = viewWidth * videoHeight / videoWidth
        }

        // 重新设置 VideoView 的宽高
        val params: LayoutParams = videoView.layoutParams
        params.width = viewWidth
        params.height = viewHeight
        videoView.layoutParams = params
        post(showControllerRunnable)
        postDelayed(refreshRunnable, (REFRESH_TIME / 2).toLong())
        listener?.onPlayStart(this)
    }

    /**
     * [MediaPlayer.OnCompletionListener]
     */
    override fun onCompletion(player: MediaPlayer?) {
        pause()
        listener?.onPlayEnd(this)
    }

    /**
     * [MediaPlayer.OnInfoListener]
     */
    override fun onInfo(player: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                lottieView.setAnimation(R.raw.progress)
                lottieView.playAnimation()
                messageView.setText(R.string.common_loading)
                post(showMessageRunnable)
                return true
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                lottieView.cancelAnimation()
                messageView.setText(R.string.common_loading)
                postDelayed(hideMessageRunnable, DIALOG_TIME.toLong())
                return true
            }
        }
        return false
    }

    /**
     * [MediaPlayer.OnErrorListener]
     */
    override fun onError(player: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (listener?.onPlayError(this, what, extra) == true) {
            return true
        }

        val activity: Activity = context.getActivity() ?: return false
        var message: String? = when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> {
                activity.getString(R.string.common_video_error_not_support)
            }
            else -> {
                activity.getString(R.string.common_video_error_unknown)
            }
        }
        message += "\n" + String.format(activity.getString(R.string.common_video_error_supplement), what, extra)
        MessageDialog.Builder(activity)
            .setMessage(message)
            .setConfirm(R.string.common_confirm)
            .setCancel(null)
            .setCancelable(false)
            .setListener(object : MessageDialog.OnListener {

                override fun onConfirm(dialog: BaseDialog) {
                    onCompletion(player)
                }
            })
            .show()
        return true
    }

    /**
     * [View.OnClickListener]
     */
    override fun onClick(view: View) {
        if (view === this) {

            // 先移除之前发送的
            removeCallbacks(showControllerRunnable)
            removeCallbacks(hideControllerRunnable)
            if (controllerShow) {
                // 隐藏控制面板
                post(hideControllerRunnable)
                return
            }

            // 显示控制面板
            post(showControllerRunnable)
            postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())

        } else if (view === backView) {

            listener?.onClickBack(this)

        } else if (view === controlView) {

            if (controlView.visibility != VISIBLE) {
                return
            }
            if (isPlaying()) {
                pause()
            } else {
                start()
            }
            // 先移除之前发送的
            removeCallbacks(showControllerRunnable)
            removeCallbacks(hideControllerRunnable)
            // 重置显示隐藏面板任务
            if (!controllerShow) {
                post(showControllerRunnable)
            }
            postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
            listener?.onClickPlay(this)

        } else if (view === lockView) {

            if (lockMode) {
                unlock()
            } else {
                lock()
            }
            listener?.onClickLock(this)
        }
    }

    fun getVideoWidth(): Int {
        return videoWidth
    }

    fun getVideoHeight(): Int {
        return videoHeight
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 满足任一条件：关闭手势控制、处于锁定状态、处于缓冲状态
        if (!gestureEnabled || lockMode || lottieView.isAnimating) {
            return super.onTouchEvent(event)
        }

        val layoutDirection = getResources().getConfiguration().getLayoutDirection()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                audioManager?.let {
                    maxVoice = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    currentVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
                }

                window?.let {
                    currentBrightnessPercent = it.attributes.screenBrightness
                    // 如果当前亮度是默认的，那么就获取系统当前的屏幕亮度
                    if (currentBrightnessPercent == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                        currentBrightnessPercent = getBrightness().toFloat() / getMaxBrightness().toFloat()
                    }
                }

                viewDownX = event.x
                viewDownY = event.y
                removeCallbacks(hideControllerRunnable)
            }
            MotionEvent.ACTION_MOVE -> run {
                // 计算偏移的距离（按下的位置 - 当前触摸的位置）
                val distanceX: Float = viewDownX - event.x
                val distanceY: Float = viewDownY - event.y
                // 手指偏移的距离一定不能太短，这个是前提条件
                if (abs(distanceY) < ViewConfiguration.get(context).scaledTouchSlop) {
                    return@run
                }
                if (touchOrientation == -1) {
                    // 判断滚动方向是垂直的还是水平的
                    if (abs(distanceY) > abs(distanceX)) {
                        touchOrientation = LinearLayout.VERTICAL
                    } else if (abs(distanceY) < abs(distanceX)) {
                        touchOrientation = LinearLayout.HORIZONTAL
                    }
                }

                // 如果手指触摸方向是水平的
                if (touchOrientation == LinearLayout.HORIZONTAL) {
                    var second: Int = (-(distanceX / width.toFloat() * 60f)).toInt()
                    if (layoutDirection == LAYOUT_DIRECTION_RTL) {
                        second = -second
                    }
                    val progress: Int = getProgress() + second * 1000
                    if (progress >= 0 && progress <= getDuration()) {
                        adjustSecond = second
                        @DrawableRes val imageResource: Int = if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                            if (adjustSecond < 0) R.drawable.video_schedule_rewind_ic else R.drawable.video_schedule_forward_ic
                        } else {
                            if (adjustSecond < 0) R.drawable.video_schedule_forward_ic else R.drawable.video_schedule_rewind_ic
                        }
                        lottieView.setImageResource(imageResource)
                        messageView.text = String.format("%s s", abs(adjustSecond))
                        post(showMessageRunnable)
                    }
                    return@run
                }

                // 如果手指触摸方向是垂直的
                if (touchOrientation == LinearLayout.VERTICAL) {
                    // 判断触摸点是在屏幕左边还是右边
                    if ((layoutDirection == LAYOUT_DIRECTION_LTR && event.x.toInt() < width / 2) ||
                        (layoutDirection == LAYOUT_DIRECTION_RTL && event.x.toInt() > width / 2)) {
                        // 手指在屏幕左边
                        val delta: Float =
                            (distanceY / height) * WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                        if (delta == 0f) {
                            return@run
                        }

                        // 更新系统亮度
                        val brightness: Float = min(max(currentBrightnessPercent + delta,
                            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF),
                            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL)
                        window?.apply {
                            val attributes: WindowManager.LayoutParams = attributes
                            attributes.screenBrightness = brightness
                            setAttributes(attributes)
                        }
                        val percent: Int = (brightness * 100).toInt()
                        @DrawableRes val iconId: Int = when {
                            percent > 100 / 3 * 2 -> {
                                R.drawable.video_brightness_high_ic
                            }
                            percent > 100 / 3 -> {
                                R.drawable.video_brightness_medium_ic
                            }
                            else -> {
                                R.drawable.video_brightness_low_ic
                            }
                        }
                        lottieView.setImageResource(iconId)
                        messageView.text = String.format("%s %%", percent)
                        post(showMessageRunnable)
                        return@run
                    }

                    // 手指在屏幕右边
                    val delta: Float = (distanceY / height) * maxVoice
                    if (delta == 0f) {
                        return@run
                    }

                    audioManager?.let {
                        // 更新系统音量
                        val voice: Int = min(max(currentVolume + delta, 0f), maxVoice.toFloat()).toInt()
                        it.setStreamVolume(AudioManager.STREAM_MUSIC, voice, 0)
                        val percent: Int = voice * 100 / maxVoice
                        @DrawableRes val iconId: Int
                        iconId = when {
                            percent > 100 / 3 * 2 -> {
                                R.drawable.video_volume_high_ic
                            }
                            percent > 100 / 3 -> {
                                R.drawable.video_volume_medium_ic
                            }
                            percent != 0 -> {
                                R.drawable.video_volume_low_ic
                            }
                            else -> {
                                R.drawable.video_volume_mute_ic
                            }
                        }
                        lottieView.setImageResource(iconId)
                        messageView.text = String.format("%s %%", percent)
                        post(showMessageRunnable)
                    }
                    return@run
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (abs(viewDownX - event.x) <= ViewConfiguration.get(context).scaledTouchSlop &&
                        abs(viewDownY - event.y) <= ViewConfiguration.get(context).scaledTouchSlop) {
                        // 如果整个视频播放区域太大，触摸移动会导致触发点击事件，所以这里换成手动派发点击事件
                        if (isEnabled && isClickable) {
                            performClick()
                        }
                    }
                }

                touchOrientation = -1
                audioManager?.let {
                    currentVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
                }
                if (adjustSecond != 0) {
                    setProgress(getProgress() + adjustSecond * 1000)
                    adjustSecond = 0
                }
                postDelayed(hideControllerRunnable, CONTROLLER_TIME.toLong())
                postDelayed(hideMessageRunnable, DIALOG_TIME.toLong())
            }
        }
        return true
    }

    /**
     * 获取屏幕当前的亮度
     */
    private fun getBrightness(): Int {
        try {
            // 这里需要注意，Settings.System.SCREEN_BRIGHTNESS 获取到的值在小米手机上面会超过 255
            return Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        // 如果没有取值成功，那么就默认设置为一半亮度，防止突然变得很亮或很暗
        return getMaxBrightness() / 2
    }

    /**
     * 获取屏幕最大显示的亮度，https://blog.csdn.net/jklwan/article/details/93669170
     */
    private fun getMaxBrightness(): Int {
        try {
            val system = Resources.getSystem()
            val resId = system.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android")
            if (resId != 0) {
                return system.getInteger(resId)
            }
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        return 255
    }

    /**
     * 点击返回监听器
     */
    interface OnPlayListener {

        /**
         * 点击了返回按钮（可在此处处理返回事件）
         */
        fun onClickBack(view: PlayerView) {
            // default implementation ignored
        }

        /**
         * 点击了锁定按钮
         */
        fun onClickLock(view: PlayerView) {
            // default implementation ignored
        }

        /**
         * 点击了播放按钮
         */
        fun onClickPlay(view: PlayerView) {
            // default implementation ignored
        }

        /**
         * 播放开始（可在此处设置播放进度）
         */
        fun onPlayStart(view: PlayerView) {
            // default implementation ignored
        }

        /**
         * 播放进度发生改变
         */
        fun onPlayProgress(view: PlayerView) {
            // default implementation ignored
        }

        /**
         * 播放结束（可在此处结束播放或者循环播放）
         */
        fun onPlayEnd(view: PlayerView) {
            // default implementation ignored
        }

        /**
         * 播放出错
         */
        fun onPlayError(view: PlayerView, what: Int, extra: Int): Boolean {
            return false
        }
    }
}