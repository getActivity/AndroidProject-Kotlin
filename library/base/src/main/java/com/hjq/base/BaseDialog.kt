package com.hjq.base

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.*
import android.graphics.drawable.Drawable
import android.os.*
import android.util.SparseArray
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.hjq.base.action.*
import java.lang.ref.SoftReference
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/11/24
 *    desc   : Dialog 技术基类
 */
@Suppress("LeakingThis")
open class BaseDialog constructor(context: Context, @StyleRes themeResId: Int = R.style.BaseDialogTheme) :
    AppCompatDialog(context, themeResId), LifecycleOwner, ActivityAction, ResourcesAction,
    HandlerAction, ClickAction, AnimAction, KeyboardAction, DialogInterface.OnShowListener,
    DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    private val listeners: ListenersWrapper<BaseDialog> = ListenersWrapper(this)
    private val lifecycle: LifecycleRegistry = LifecycleRegistry(this)
    private var showListeners: MutableList<OnShowListener?>? = null
    private var cancelListeners: MutableList<OnCancelListener?>? = null
    private var dismissListeners: MutableList<OnDismissListener?>? = null

    /**
     * 获取 Dialog 的根布局
     */
    open fun getContentView(): View? {
        val contentView: View? = findViewById(Window.ID_ANDROID_CONTENT)
        if (contentView is ViewGroup && contentView.childCount == 1) {
            return contentView.getChildAt(0)
        }
        return contentView
    }

    /**
     * 设置 Dialog 宽度
     */
    open fun setWidth(width: Int) {
        val window: Window = window ?: return
        val params: WindowManager.LayoutParams? = window.attributes
        params?.width = width
        window.attributes = params
    }

    /**
     * 设置 Dialog 高度
     */
    open fun setHeight(height: Int) {
        val window: Window = window ?: return
        val params: WindowManager.LayoutParams? = window.attributes
        params?.height = height
        window.attributes = params
    }

    /**
     * 设置水平偏移
     */
    open fun setXOffset(offset: Int) {
        val window: Window = window ?: return
        val params: WindowManager.LayoutParams? = window.attributes
        params?.x = offset
        window.attributes = params
    }

    /**
     * 设置垂直偏移
     */
    open fun setYOffset(offset: Int) {
        val window: Window = window ?: return
        val params: WindowManager.LayoutParams? = window.attributes
        params?.y = offset
        window.attributes = params
    }

    /**
     * 获取 Dialog 重心
     */
    open fun getGravity(): Int {
        val window: Window = window ?: return Gravity.NO_GRAVITY
        val params: WindowManager.LayoutParams = window.attributes ?: return Gravity.NO_GRAVITY
        return params.gravity
    }

    /**
     * 设置 Dialog 重心
     */
    open fun setGravity(gravity: Int) {
        window?.setGravity(gravity)
    }

    /**
     * 设置 Dialog 的动画
     */
    open fun setWindowAnimations(@StyleRes id: Int) {
        window?.setWindowAnimations(id)
    }

    /**
     * 获取 Dialog 的动画
     */
    open fun getWindowAnimations(): Int {
        val window: Window = window ?: return AnimAction.ANIM_DEFAULT
        return window.attributes.windowAnimations
    }

    /**
     * 设置背景遮盖层开关
     */
    open fun setBackgroundDimEnabled(enabled: Boolean) {
        if (enabled) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    /**
     * 设置背景遮盖层的透明度（前提条件是背景遮盖层开关必须是为开启状态）
     */
    open fun setBackgroundDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float) {
        window?.setDimAmount(dimAmount)
    }

    override fun dismiss() {
        removeCallbacks()
        val focusView: View? = currentFocus
        if (focusView != null) {
            getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(focusView.windowToken, 0)
        }
        super.dismiss()
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    /**
     * 设置一个显示监听器
     *
     * @param listener       显示监听器对象
     */
    @Deprecated("请使用 {@link #addOnShowListener(BaseDialog.OnShowListener)}}")
    override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
        if (listener == null) {
            return
        }
        addOnShowListener(ShowListenerWrapper(listener))
    }

    /**
     * 设置一个取消监听器
     *
     * @param listener       取消监听器对象
     */
    @Deprecated("请使用 {@link #addOnCancelListener(BaseDialog.OnCancelListener)}")
    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        if (listener == null) {
            return
        }
        addOnCancelListener(CancelListenerWrapper(listener))
    }

    /**
     * 设置一个销毁监听器
     *
     * @param listener       销毁监听器对象
     */
    @Deprecated("请使用 {@link #addOnDismissListener(BaseDialog.OnDismissListener)}")
    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        if (listener == null) {
            return
        }
        addOnDismissListener(DismissListenerWrapper(listener))
    }

    /**
     * 设置一个按键监听器
     *
     * @param listener       按键监听器对象
     */
    @Deprecated("请使用 {@link #setOnKeyListener(BaseDialog.OnKeyListener)}", ReplaceWith(
        "super.setOnKeyListener(listener)",
        "androidx.appcompat.app.AppCompatDialog"))
    override fun setOnKeyListener(listener: DialogInterface.OnKeyListener?) {
        super.setOnKeyListener(listener)
    }

    open fun setOnKeyListener(listener: OnKeyListener?) {
        super.setOnKeyListener(KeyListenerWrapper(listener))
    }

    /**
     * 添加一个显示监听器
     *
     * @param listener      监听器对象
     */
    open fun addOnShowListener(listener: OnShowListener?) {
        if (showListeners == null) {
            showListeners = ArrayList()
            super.setOnShowListener(listeners)
        }
        showListeners?.add(listener)
    }

    /**
     * 添加一个取消监听器
     *
     * @param listener      监听器对象
     */
    open fun addOnCancelListener(listener: OnCancelListener?) {
        if (cancelListeners == null) {
            cancelListeners = ArrayList()
            super.setOnCancelListener(listeners)
        }
        cancelListeners?.add(listener)
    }

    /**
     * 添加一个销毁监听器
     *
     * @param listener      监听器对象
     */
    open fun addOnDismissListener(listener: OnDismissListener?) {
        if (dismissListeners == null) {
            dismissListeners = ArrayList()
            super.setOnDismissListener(listeners)
        }
        dismissListeners?.add(listener)
    }

    /**
     * 移除一个显示监听器
     *
     * @param listener      监听器对象
     */
    open fun removeOnShowListener(listener: OnShowListener?) {
        showListeners?.remove(listener)
    }

    /**
     * 移除一个取消监听器
     *
     * @param listener      监听器对象
     */
    open fun removeOnCancelListener(listener: OnCancelListener?) {
        cancelListeners?.remove(listener)
    }

    /**
     * 移除一个销毁监听器
     *
     * @param listener      监听器对象
     */
    open fun removeOnDismissListener(listener: OnDismissListener?) {
        dismissListeners?.remove(listener)
    }

    /**
     * 设置显示监听器集合
     */
    private fun setOnShowListeners(listeners: MutableList<OnShowListener?>?) {
        super.setOnShowListener(this.listeners)
        showListeners = listeners
    }

    /**
     * 设置取消监听器集合
     */
    private fun setOnCancelListeners(listeners: MutableList<OnCancelListener?>?) {
        super.setOnCancelListener(this.listeners)
        cancelListeners = listeners
    }

    /**
     * 设置销毁监听器集合
     */
    private fun setOnDismissListeners(listeners: MutableList<OnDismissListener?>?) {
        super.setOnDismissListener(this.listeners)
        dismissListeners = listeners
    }

    /**
     * [DialogInterface.OnShowListener]
     */
    override fun onShow(dialog: DialogInterface?) {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        showListeners?.let {
            for (i in it.indices) {
                it[i]?.onShow(this)
            }
        }
    }

    /**
     * [DialogInterface.OnCancelListener]
     */
    override fun onCancel(dialog: DialogInterface?) {
        cancelListeners?.let {
            for (i in it.indices) {
                it[i]?.onCancel(this)
            }
        }
    }

    /**
     * [DialogInterface.OnDismissListener]
     */
    override fun onDismiss(dialog: DialogInterface?) {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        dismissListeners?.let {
            for (i in it.indices) {
                it[i]?.onDismiss(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart() {
        super.onStart()
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onStop() {
        super.onStop()
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @Suppress("UNCHECKED_CAST")
    open class Builder<B : Builder<B>> constructor(private val context: Context) :
        ActivityAction, ResourcesAction, ClickAction, KeyboardAction {

        /** Dialog 对象 */
        private var dialog: BaseDialog? = null

        /** Dialog 布局 */
        private var contentView: View? = null

        /** 主题样式 */
        private var themeId: Int = R.style.BaseDialogTheme

        /** 动画样式 */
        private var animStyle: Int = AnimAction.ANIM_DEFAULT

        /** 宽度和高度 */
        private var width: Int = WindowManager.LayoutParams.WRAP_CONTENT
        private var height: Int = WindowManager.LayoutParams.WRAP_CONTENT

        /** 重心位置 */
        private var gravity: Int = Gravity.NO_GRAVITY

        /** 水平偏移 */
        private var xOffset: Int = 0

        /** 垂直偏移 */
        private var yOffset: Int = 0

        /** 是否能够被取消 */
        private var cancelable: Boolean = true

        /** 点击空白是否能够取消  前提是这个对话框可以被取消 */
        private var canceledOnTouchOutside: Boolean = true

        /** 背景遮盖层开关 */
        private var backgroundDimEnabled: Boolean = true

        /** 背景遮盖层透明度 */
        private var backgroundDimAmount: Float = 0.5f

        /** Dialog 创建监听 */
        private var createListener: OnCreateListener? = null

        /** Dialog 显示监听 */
        private val showListeners: MutableList<OnShowListener?> by lazy { ArrayList() }

        /** Dialog 取消监听 */
        private val cancelListeners: MutableList<OnCancelListener?> by lazy { ArrayList() }

        /** Dialog 销毁监听 */
        private val dismissListeners: MutableList<OnDismissListener?> by lazy { ArrayList() }

        /** Dialog 按键监听 */
        private var keyListener: OnKeyListener? = null

        /** 点击事件集合 */
        private var clickArray: SparseArray<OnClickListener<View>?>? = null

        /**
         * 设置布局
         */
        open fun setContentView(@LayoutRes id: Int): B {
            // 这里解释一下，为什么要传 new FrameLayout，因为如果不传的话，XML 的根布局获取到的 LayoutParams 对象会为空，也就会导致宽高参数解析不出来
            return setContentView(
                LayoutInflater.from(context).inflate(id, FrameLayout(context), false)
            )
        }

        open fun setContentView(view: View?): B {
            // 请不要传入空的布局
            if (view == null) {
                throw IllegalArgumentException("are you ok?")
            }
            contentView = view
            if (isCreated()) {
                dialog?.setContentView(view)
                return this as B
            }
            val layoutParams: ViewGroup.LayoutParams? = contentView?.layoutParams
            if ((layoutParams != null) && (width == ViewGroup.LayoutParams.WRAP_CONTENT) && (height == ViewGroup.LayoutParams.WRAP_CONTENT)) {
                // 如果当前 Dialog 的宽高设置了自适应，就以布局中设置的宽高为主
                setWidth(layoutParams.width)
                setHeight(layoutParams.height)
            }

            // 如果当前没有设置重心，就自动获取布局重心
            if (gravity == Gravity.NO_GRAVITY) {
                if (layoutParams is FrameLayout.LayoutParams) {
                    val gravity: Int = layoutParams.gravity
                    if (gravity != FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY) {
                        setGravity(gravity)
                    }
                } else if (layoutParams is LinearLayout.LayoutParams) {
                    val gravity: Int = layoutParams.gravity
                    if (gravity != Gravity.NO_GRAVITY) {
                        setGravity(gravity)
                    }
                }
                if (gravity == Gravity.NO_GRAVITY) {
                    // 默认重心是居中
                    setGravity(Gravity.CENTER)
                }
            }
            return this as B
        }

        /**
         * 设置主题 id
         */
        open fun setThemeStyle(@StyleRes id: Int): B {
            themeId = id
            if (isCreated()) {
                // Dialog 创建之后不能再设置主题 id
                throw IllegalStateException("are you ok?")
            }
            return this as B
        }

        /**
         * 设置动画，已经封装好几种样式，具体可见[AnimAction]类
         */
        open fun setAnimStyle(@StyleRes id: Int): B {
            animStyle = id
            if (isCreated()) {
                dialog?.setWindowAnimations(id)
            }
            return this as B
        }

        /**
         * 设置宽度
         */
        open fun setWidth(width: Int): B {
            this.width = width
            if (isCreated()) {
                dialog?.setWidth(width)
                return this as B
            }

            // 这里解释一下为什么要重新设置 LayoutParams
            // 因为如果不这样设置的话，第一次显示的时候会按照 Dialog 宽高显示
            // 但是 Layout 内容变更之后就不会按照之前的设置宽高来显示
            // 所以这里我们需要对 View 的 LayoutParams 也进行设置

            val params: ViewGroup.LayoutParams? = contentView?.layoutParams
            if (params != null) {
                params.width = width
                contentView?.layoutParams = params
            }
            return this as B
        }

        /**
         * 设置高度
         */
        open fun setHeight(height: Int): B {
            this.height = height
            if (isCreated()) {
                dialog?.setHeight(height)
                return this as B
            }

            // 这里解释一下为什么要重新设置 LayoutParams
            // 因为如果不这样设置的话，第一次显示的时候会按照 Dialog 宽高显示
            // 但是 Layout 内容变更之后就不会按照之前的设置宽高来显示
            // 所以这里我们需要对 View 的 LayoutParams 也进行设置
            val params: ViewGroup.LayoutParams? = contentView?.layoutParams
            if (params != null) {
                params.height = height
                contentView?.layoutParams = params
            }
            return this as B
        }

        /**
         * 设置重心位置
         */
        open fun setGravity(gravity: Int): B {
            // 适配布局反方向
            this.gravity = Gravity.getAbsoluteGravity(gravity, getResources().configuration.layoutDirection)
            if (isCreated()) {
                dialog?.setGravity(gravity)
            }
            return this as B
        }

        /**
         * 设置水平偏移
         */
        open fun setXOffset(offset: Int): B {
            xOffset = offset
            if (isCreated()) {
                dialog?.setXOffset(offset)
            }
            return this as B
        }

        /**
         * 设置垂直偏移
         */
        open fun setYOffset(offset: Int): B {
            yOffset = offset
            if (isCreated()) {
                this.dialog?.setYOffset(offset)
            }
            return this as B
        }

        /**
         * 是否可以取消
         */
        open fun setCancelable(cancelable: Boolean): B {
            this.cancelable = cancelable
            if (isCreated()) {
                dialog?.setCancelable(cancelable)
            }
            return this as B
        }

        /**
         * 是否可以通过点击空白区域取消
         */
        open fun setCanceledOnTouchOutside(cancel: Boolean): B {
            canceledOnTouchOutside = cancel
            if (isCreated() && cancelable) {
                dialog?.setCanceledOnTouchOutside(cancel)
            }
            return this as B
        }

        /**
         * 设置背景遮盖层开关
         */
        open fun setBackgroundDimEnabled(enabled: Boolean): B {
            backgroundDimEnabled = enabled
            if (isCreated()) {
                dialog?.setBackgroundDimEnabled(enabled)
            }
            return this as B
        }

        /**
         * 设置背景遮盖层的透明度（前提条件是背景遮盖层开关必须是为开启状态）
         */
        open fun setBackgroundDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float): B {
            backgroundDimAmount = dimAmount
            if (isCreated()) {
                dialog?.setBackgroundDimAmount(dimAmount)
            }
            return this as B
        }

        /**
         * 设置创建监听
         */
        open fun setOnCreateListener(listener: OnCreateListener): B {
            createListener = listener
            return this as B
        }

        /**
         * 添加显示监听
         */
        open fun addOnShowListener(listener: OnShowListener): B {
            showListeners.add(listener)
            return this as B
        }

        /**
         * 添加取消监听
         */
        open fun addOnCancelListener(listener: OnCancelListener): B {
            cancelListeners.add(listener)
            return this as B
        }

        /**
         * 添加销毁监听
         */
        open fun addOnDismissListener(listener: OnDismissListener): B {
            dismissListeners.add(listener)
            return this as B
        }

        /**
         * 设置按键监听
         */
        open fun setOnKeyListener(listener: OnKeyListener): B {
            keyListener = listener
            if (isCreated()) {
                dialog?.setOnKeyListener(listener)
            }
            return this as B
        }

        /**
         * 设置文本
         */
        open fun setText(@IdRes viewId: Int, @StringRes stringId: Int): B {
            return setText(viewId, getString(stringId))
        }

        open fun setText(@IdRes id: Int, text: CharSequence?): B {
            (findViewById<View?>(id) as TextView?)?.text = text
            return this as B
        }

        /**
         * 设置文本颜色
         */
        open fun setTextColor(@IdRes id: Int, @ColorInt color: Int): B {
            (findViewById<View?>(id) as TextView?)?.setTextColor(color)
            return this as B
        }

        /**
         * 设置提示
         */
        open fun setHint(@IdRes viewId: Int, @StringRes stringId: Int): B {
            return setHint(viewId, getString(stringId))
        }

        open fun setHint(@IdRes id: Int, text: CharSequence?): B {
            (findViewById<View?>(id) as TextView?)?.hint = text
            return this as B
        }

        /**
         * 设置可见状态
         */
        open fun setVisibility(@IdRes id: Int, visibility: Int): B {
            findViewById<View?>(id)?.visibility = visibility
            return this as B
        }

        /**
         * 设置背景
         */
        open fun setBackground(@IdRes viewId: Int, @DrawableRes drawableId: Int): B {
            return setBackground(viewId, ContextCompat.getDrawable(context, drawableId))
        }

        open fun setBackground(@IdRes id: Int, drawable: Drawable?): B {
            findViewById<View?>(id)?.background = drawable
            return this as B
        }

        /**
         * 设置图片
         */
        open fun setImageDrawable(@IdRes viewId: Int, @DrawableRes drawableId: Int): B {
            return setBackground(viewId, ContextCompat.getDrawable(context, drawableId))
        }

        open fun setImageDrawable(@IdRes id: Int, drawable: Drawable?): B {
            (findViewById<View?>(id) as ImageView?)?.setImageDrawable(drawable)
            return this as B
        }

        /**
         * 设置点击事件
         */
        open fun setOnClickListener(@IdRes id: Int, listener: OnClickListener<out View>): B {
            if (clickArray == null) {
                clickArray = SparseArray()
            }
            clickArray!!.put(id, listener as OnClickListener<View>)
            if (isCreated()) {
                dialog?.findViewById<View?>(id)?.setOnClickListener(ViewClickWrapper(dialog, listener))
            }
            return this as B
        }

        /**
         * 创建
         */
        @Suppress("RtlHardcoded")
        open fun create(): BaseDialog {
            // 判断布局是否为空
            if (contentView == null) {
                throw IllegalArgumentException("are you ok?")
            }

            // 如果当前正在显示
            if (isShowing()) {
                dismiss()
            }

            // 如果当前没有设置重心，就设置一个默认的重心
            if (gravity == Gravity.NO_GRAVITY) {
                gravity = Gravity.CENTER
            }

            // 如果当前没有设置动画效果，就设置一个默认的动画效果
            if (animStyle == AnimAction.ANIM_DEFAULT) {
                animStyle = when (gravity) {
                    Gravity.TOP -> AnimAction.ANIM_TOP
                    Gravity.BOTTOM -> AnimAction.ANIM_BOTTOM
                    Gravity.LEFT -> AnimAction.ANIM_LEFT
                    Gravity.RIGHT -> AnimAction.ANIM_RIGHT
                    else -> AnimAction.ANIM_DEFAULT
                }
            }

            // 创建新的 Dialog 对象
            dialog = createDialog(context, themeId)
            dialog!!.let { dialog ->
                dialog.setContentView(contentView!!)
                dialog.setCancelable(cancelable)
                if (cancelable) {
                    dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
                }
                dialog.setOnShowListeners(showListeners)
                dialog.setOnCancelListeners(cancelListeners)
                dialog.setOnDismissListeners(dismissListeners)
                dialog.setOnKeyListener(keyListener)
                val window: Window? = dialog.window
                if (window != null) {
                    val params: WindowManager.LayoutParams = window.attributes
                    params.width = width
                    params.height = height
                    params.gravity = gravity
                    params.x = xOffset
                    params.y = yOffset
                    params.windowAnimations = animStyle

                    if (backgroundDimEnabled) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        window.setDimAmount(backgroundDimAmount)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    }
                    window.attributes = params
                }

                clickArray?.let { array ->
                    var i = 0
                    while (i < array.size()) {
                        contentView!!.findViewById<View?>(array.keyAt(i))?.
                        setOnClickListener(ViewClickWrapper(dialog, array.valueAt(i)))
                        i++
                    }
                }

                getActivity()?.let { activity ->
                    // 将 Dialog 的生命周期和 Activity 绑定在一起
                    DialogLifecycle.with(activity, dialog)
                }
                createListener?.onCreate(dialog)
            }
            return dialog!!
        }

        /**
         * 显示
         */
        open fun show() {
            val activity = getActivity()
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                return
            }
            if (!isCreated()) {
                create()
            }
            if (isShowing()) {
                return
            }
            dialog?.show()
        }

        /**
         * 销毁当前 Dialog
         */
        open fun dismiss() {
            val activity = getActivity()
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                return
            }
            dialog?.dismiss()
        }

        override fun getContext(): Context {
            return context
        }

        /**
         * 当前 Dialog 是否创建了
         */
        open fun isCreated(): Boolean {
            return dialog != null
        }

        /**
         * 当前 Dialog 是否显示了
         */
        open fun isShowing(): Boolean {
            return isCreated() && dialog!!.isShowing
        }

        /**
         * 创建 Dialog 对象（子类可以重写此方法来改变 Dialog 类型）
         */
        protected open fun createDialog(context: Context, @StyleRes themeId: Int): BaseDialog {
            return BaseDialog(context, themeId)
        }

        /**
         * 延迟执行
         */
        open fun post(runnable: Runnable) {
            if (isShowing()) {
                dialog?.post(runnable)
            } else {
                addOnShowListener(ShowPostWrapper(runnable))
            }
        }

        /**
         * 延迟一段时间执行
         */
        open fun postDelayed(runnable: Runnable, delayMillis: Long) {
            if (isShowing()) {
                dialog?.postDelayed(runnable, delayMillis)
            } else {
                addOnShowListener(ShowPostDelayedWrapper(runnable, delayMillis))
            }
        }

        /**
         * 在指定的时间执行
         */
        open fun postAtTime(runnable: Runnable, uptimeMillis: Long) {
            if (isShowing()) {
                dialog?.postAtTime(runnable, uptimeMillis)
            } else {
                addOnShowListener(ShowPostAtTimeWrapper(runnable, uptimeMillis))
            }
        }

        /**
         * 获取 Dialog 的根布局
         */
        open fun getContentView(): View? {
            return contentView
        }

        /**
         * 根据 id 查找 View
         */
        override fun <V : View?> findViewById(@IdRes id: Int): V? {
            if (contentView == null) {
                // 没有 setContentView 就想 findViewById ?
                throw IllegalStateException("are you ok?")
            }
            return contentView!!.findViewById(id)
        }

        /**
         * 获取当前 Dialog 对象
         */
        open fun getDialog(): BaseDialog? {
            return dialog
        }
    }

    /**
     * Dialog 生命周期绑定
     */
    private class DialogLifecycle(private var activity: Activity?, private var dialog: BaseDialog?) :
        ActivityLifecycleCallbacks, OnShowListener, OnDismissListener {

        companion object {

            fun with(activity: Activity, dialog: BaseDialog?) {
                DialogLifecycle(activity, dialog)
            }
        }

        init {
            this.dialog?.addOnShowListener(this)
            this.dialog?.addOnDismissListener(this)
        }

        /** Dialog 动画样式（避免 Dialog 从后台返回到前台后再次触发动画效果） */
        private var dialogAnim: Int = 0

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            if (activity !== activity) {
                return
            }

            dialog?.let {
                if (!it.isShowing) {
                    return
                }

                // 还原 Dialog 动画样式（这里必须要使用延迟设置，否则还是有一定几率会出现）
                it.postDelayed({
                    if (!it.isShowing) {
                        return@postDelayed
                    }
                    it.setWindowAnimations(dialogAnim)
                }, 100)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (this.activity !== activity) {
                return
            }

            this.dialog?.let {
                if (!it.isShowing) {
                    return
                }

                // 获取 Dialog 动画样式
                dialogAnim = it.getWindowAnimations()
                // 设置 Dialog 无动画效果
                it.setWindowAnimations(AnimAction.ANIM_EMPTY)
            }
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (this.activity !== activity) {
                return
            }
            unregisterActivityLifecycleCallbacks()
            this.activity = null
            this.dialog?.let {
                it.removeOnShowListener(this)
                it.removeOnDismissListener(this)
                if (it.isShowing) {
                    it.dismiss()
                }
            }
            this.dialog = null
        }

        override fun onShow(dialog: BaseDialog?) {
            this.dialog = dialog
            registerActivityLifecycleCallbacks()
        }

        override fun onDismiss(dialog: BaseDialog?) {
            this.dialog = null
            unregisterActivityLifecycleCallbacks()
        }

        /**
         * 注册 Activity 生命周期监听
         */
        private fun registerActivityLifecycleCallbacks() {
            activity?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.registerActivityLifecycleCallbacks(this)
                } else {
                    it.application.registerActivityLifecycleCallbacks(this)
                }
            }
        }

        /**
         * 反注册 Activity 生命周期监听
         */
        private fun unregisterActivityLifecycleCallbacks() {
            activity?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.unregisterActivityLifecycleCallbacks(this)
                } else {
                    it.application.unregisterActivityLifecycleCallbacks(this)
                }
            }
        }
    }

    /**
     * Dialog 监听包装类（修复原生 Dialog 监听器对象导致的内存泄漏）
     */
    private class ListenersWrapper<T>(referent: T?) :
        SoftReference<T?>(referent), DialogInterface.OnShowListener,
        DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener
            where T : DialogInterface.OnShowListener,
                  T : DialogInterface.OnCancelListener,
                  T : DialogInterface.OnDismissListener {

        override fun onShow(dialog: DialogInterface?) {
            get()?.onShow(dialog)
        }

        override fun onCancel(dialog: DialogInterface?) {
            get()?.onCancel(dialog)
        }

        override fun onDismiss(dialog: DialogInterface?) {
            get()?.onDismiss(dialog)
        }
    }

    /**
     * 点击事件包装类
     */
    private class ViewClickWrapper constructor(
        private val dialog: BaseDialog?,
        private val listener: OnClickListener<View>?) : View.OnClickListener {

        override fun onClick(view: View) {
            listener?.onClick(dialog, view)
        }
    }

    /**
     * 显示监听包装类
     */
    private class ShowListenerWrapper constructor(referent: DialogInterface.OnShowListener?) :
        SoftReference<DialogInterface.OnShowListener?>(referent), OnShowListener {

        override fun onShow(dialog: BaseDialog?) {
            // 在横竖屏切换后监听对象会为空
            get()?.onShow(dialog)
        }
    }

    /**
     * 取消监听包装类
     */
    private class CancelListenerWrapper constructor(referent: DialogInterface.OnCancelListener?) :
        SoftReference<DialogInterface.OnCancelListener?>(referent), OnCancelListener {

        override fun onCancel(dialog: BaseDialog?) {
            // 在横竖屏切换后监听对象会为空
            get()?.onCancel(dialog)
        }
    }

    /**
     * 销毁监听包装类
     */
    private class DismissListenerWrapper constructor(referent: DialogInterface.OnDismissListener?) :
        SoftReference<DialogInterface.OnDismissListener?>(referent), OnDismissListener {

        override fun onDismiss(dialog: BaseDialog?) {
            // 在横竖屏切换后监听对象会为空
            get()?.onDismiss(dialog)
        }
    }

    /**
     * 按键监听包装类
     */
    private class KeyListenerWrapper constructor(private val listener: OnKeyListener?) : DialogInterface.OnKeyListener {

        override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
            // 在横竖屏切换后监听对象会为空
            if (listener == null || dialog !is BaseDialog) {
                return false
            }
            return listener.onKey(dialog, event)
        }
    }

    /**
     * post 任务包装类
     */
    private class ShowPostWrapper constructor(private val runnable: Runnable?) : OnShowListener {

        override fun onShow(dialog: BaseDialog?) {
            if (runnable == null) {
                return
            }
            dialog?.removeOnShowListener(this)
            dialog?.post(runnable)
        }
    }

    /**
     * postDelayed 任务包装类
     */
    private class ShowPostDelayedWrapper constructor(
        private val runnable: Runnable?,
        private val delayMillis: Long) : OnShowListener {

        override fun onShow(dialog: BaseDialog?) {
            if (runnable == null) {
                return
            }
            dialog?.removeOnShowListener(this)
            dialog?.postDelayed(runnable, delayMillis)
        }
    }

    /**
     * postAtTime 任务包装类
     */
    private class ShowPostAtTimeWrapper constructor(
        private val runnable: Runnable,
        private val uptimeMillis: Long) : OnShowListener {

        override fun onShow(dialog: BaseDialog?) {
            dialog?.removeOnShowListener(this)
            dialog?.postAtTime(runnable, uptimeMillis)
        }
    }

    /**
     * 点击监听器
     */
    interface OnClickListener<V : View> {

        /**
         * 点击事件触发了
         */
        fun onClick(dialog: BaseDialog?, view: V)
    }

    /**
     * 创建监听器
     */
    interface OnCreateListener {

        /**
         * Dialog 创建了
         */
        fun onCreate(dialog: BaseDialog?)
    }

    /**
     * 显示监听器
     */
    interface OnShowListener {

        /**
         * Dialog 显示了
         */
        fun onShow(dialog: BaseDialog?)
    }

    /**
     * 取消监听器
     */
    interface OnCancelListener {

        /**
         * Dialog 取消了
         */
        fun onCancel(dialog: BaseDialog?)
    }

    /**
     * 销毁监听器
     */
    interface OnDismissListener {

        /**
         * Dialog 销毁了
         */
        fun onDismiss(dialog: BaseDialog?)
    }

    /**
     * 按键监听器
     */
    interface OnKeyListener {

        /**
         * 触发了按键
         */
        fun onKey(dialog: BaseDialog?, event: KeyEvent?): Boolean
    }
}