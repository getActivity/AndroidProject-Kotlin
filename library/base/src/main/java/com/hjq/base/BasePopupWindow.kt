package com.hjq.base

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.widget.PopupWindowCompat
import com.hjq.base.action.*
import java.lang.ref.SoftReference
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/09/16
 *    desc   : PopupWindow 技术基类
 */
open class BasePopupWindow constructor(private val context: Context) : PopupWindow(context), ActivityAction,
    HandlerAction, ClickAction, AnimAction, KeyboardAction, PopupWindow.OnDismissListener {

    private var popupBackground: PopupBackground? = null
    private var showListeners: MutableList<OnShowListener?>? = null
    private var dismissListeners: MutableList<OnDismissListener?>? = null

    override fun getContext(): Context {
        return context
    }

    /**
     * 设置一个销毁监听器
     *
     * @param listener       销毁监听器对象
     */
    @Deprecated("请使用 {@link #addOnDismissListener(BasePopupWindow.OnDismissListener)}")
    override fun setOnDismissListener(listener: PopupWindow.OnDismissListener?) {
        if (listener == null) {
            return
        }
        addOnDismissListener(DismissListenerWrapper(listener))
    }

    /**
     * 添加一个显示监听器
     *
     * @param listener      监听器对象
     */
    open fun addOnShowListener(listener: OnShowListener?) {
        if (showListeners == null) {
            showListeners = ArrayList()
        }
        showListeners!!.add(listener)
    }

    /**
     * 添加一个销毁监听器
     *
     * @param listener      监听器对象
     */
    open fun addOnDismissListener(listener: OnDismissListener?) {
        if (dismissListeners == null) {
            dismissListeners = ArrayList()
            super.setOnDismissListener(this)
        }
        dismissListeners!!.add(listener)
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
        showListeners = listeners
    }

    /**
     * 设置销毁监听器集合
     */
    private fun setOnDismissListeners(listeners: MutableList<OnDismissListener?>?) {
        super.setOnDismissListener(this)
        dismissListeners = listeners
    }

    /**
     * [PopupWindow.OnDismissListener]
     */
    override fun onDismiss() {
        if (dismissListeners == null) {
            return
        }
        for (listener: OnDismissListener? in dismissListeners!!) {
            listener?.onDismiss(this)
        }
    }

    override fun showAsDropDown(anchor: View?, xOff: Int, yOff: Int, gravity: Int) {
        if (isShowing || contentView == null) {
            return
        }
        if (showListeners != null) {
            for (listener: OnShowListener? in showListeners!!) {
                listener?.onShow(this)
            }
        }
        super.showAsDropDown(anchor, xOff, yOff, gravity)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        if (isShowing || contentView == null) {
            return
        }
        if (showListeners != null) {
            for (listener: OnShowListener? in showListeners!!) {
                listener?.onShow(this)
            }
        }
        super.showAtLocation(parent, gravity, x, y)
    }

    override fun dismiss() {
        super.dismiss()
        removeCallbacks()
    }

    override fun <V : View?> findViewById(@IdRes id: Int): V? {
        return contentView.findViewById(id)
    }

    override fun setWindowLayoutType(type: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.setWindowLayoutType(type)
        } else {
            PopupWindowCompat.setWindowLayoutType(this, type)
        }
    }

    override fun getWindowLayoutType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.getWindowLayoutType()
        } else {
            PopupWindowCompat.getWindowLayoutType(this)
        }
    }

    override fun setOverlapAnchor(overlapAnchor: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.setOverlapAnchor(overlapAnchor)
        } else {
            PopupWindowCompat.setOverlapAnchor(this, overlapAnchor)
        }
    }

    /**
     * 设置背景遮盖层的透明度
     */
    open fun setBackgroundDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float) {
        val alpha: Float = 1 - dimAmount
        if (isShowing) {
            setActivityAlpha(alpha)
        }
        if (popupBackground == null && alpha != 1f) {
            popupBackground = PopupBackground()
            addOnShowListener(popupBackground)
            addOnDismissListener(popupBackground)
        }
        if (popupBackground != null) {
            popupBackground?.setAlpha(alpha)
        }
    }

    /**
     * 设置 Activity 窗口透明度
     */
    private fun setActivityAlpha(alpha: Float) {
        val activity: Activity = getActivity() ?: return
        val params: WindowManager.LayoutParams = activity.window.attributes
        val animator: ValueAnimator = ValueAnimator.ofFloat(params.alpha, alpha)
        animator.duration = 300
        animator.addUpdateListener { animation: ValueAnimator ->
            val value: Float = animation.animatedValue as Float
            if (value != params.alpha) {
                params.alpha = value
                activity.window.attributes = params
            }
        }
        animator.start()
    }

    @Suppress("UNCHECKED_CAST")
    open class Builder<B : Builder<B>> constructor(
        private val context: Context
    ) : ActivityAction, ResourcesAction, ClickAction, KeyboardAction {

        companion object {
            private const val DEFAULT_ANCHORED_GRAVITY: Int = Gravity.TOP or Gravity.START
        }
        
        /** PopupWindow 对象 */
        private var popupWindow: BasePopupWindow? = null

        /** PopupWindow 布局 */
        private var contentView: View? = null

        /** 动画样式 */
        private var animStyle: Int = AnimAction.ANIM_DEFAULT

        /** 宽度和高度 */
        private var width: Int = WindowManager.LayoutParams.WRAP_CONTENT
        private var height: Int = WindowManager.LayoutParams.WRAP_CONTENT

        /** 重心位置 */
        private var gravity: Int = DEFAULT_ANCHORED_GRAVITY

        /** 水平偏移 */
        private var xOffset: Int = 0

        /** 垂直偏移 */
        private var yOffset: Int = 0

        /** 是否可触摸 */
        private var touchable: Boolean = true

        /** 是否有焦点 */
        private var focusable: Boolean = true

        /** 是否外层可触摸 */
        private var outsideTouchable: Boolean = false

        /** 背景遮盖层透明度 */
        private var backgroundDimAmount: Float = 0f

        /** PopupWindow 创建监听 */
        private var createListener: OnCreateListener? = null

        /** PopupWindow 显示监听 */
        private val showListeners: MutableList<OnShowListener?> by lazy { ArrayList() }

        /** PopupWindow 销毁监听 */
        private val dismissListeners: MutableList<OnDismissListener?> by lazy { ArrayList() }

        /** 点击事件集合 */
        private var clickArray: SparseArray<OnClickListener<View>>? = null

        /**
         * 设置布局
         */
        open fun setContentView(@LayoutRes id: Int): B {
            // 这里解释一下，为什么要传 new FrameLayout，因为如果不传的话，XML 的根布局获取到的 LayoutParams 对象会为空，也就会导致宽高解析不出来
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
                popupWindow!!.contentView = view
                return this as B
            }
            val layoutParams: ViewGroup.LayoutParams? = contentView!!.layoutParams
            if ((layoutParams != null) && (width == ViewGroup.LayoutParams.WRAP_CONTENT) && (height == ViewGroup.LayoutParams.WRAP_CONTENT)) {
                // 如果当前 PopupWindow 的宽高设置了自适应，就以布局中设置的宽高为主
                setWidth(layoutParams.width)
                setHeight(layoutParams.height)
            }

            // 如果当前没有设置重心，就自动获取布局重心
            if (gravity == DEFAULT_ANCHORED_GRAVITY) {
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
         * 设置动画，已经封装好几种样式，具体可见[AnimAction]类
         */
        open fun setAnimStyle(@StyleRes id: Int): B {
            animStyle = id
            if (isCreated()) {
                popupWindow?.animationStyle = id
            }
            return this as B
        }

        /**
         * 设置宽度
         */
        open fun setWidth(width: Int): B {
            this.width = width
            if (isCreated()) {
                popupWindow!!.width = width
                return this as B
            }
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
                popupWindow!!.height = height
                return this as B
            }

            // 这里解释一下为什么要重新设置 LayoutParams
            // 因为如果不这样设置的话，第一次显示的时候会按照 PopupWindow 宽高显示
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
            return this as B
        }

        /**
         * 设置水平偏移量
         */
        open fun setXOffset(offset: Int): B {
            xOffset = offset
            return this as B
        }

        /**
         * 设置垂直偏移量
         */
        open fun setYOffset(offset: Int): B {
            yOffset = offset
            return this as B
        }

        /**
         * 是否可触摸
         */
        open fun setTouchable(touchable: Boolean): B {
            this.touchable = touchable
            if (isCreated()) {
                popupWindow!!.isTouchable = touchable
            }
            return this as B
        }

        /**
         * 是否有焦点
         */
        open fun setFocusable(focusable: Boolean): B {
            this.focusable = focusable
            if (isCreated()) {
                popupWindow!!.isFocusable = focusable
            }
            return this as B
        }

        /**
         * 是否外层可触摸
         */
        open fun setOutsideTouchable(outsideTouchable: Boolean): B {
            this.outsideTouchable = outsideTouchable
            if (isCreated()) {
                popupWindow!!.isOutsideTouchable = outsideTouchable
            }
            return this as B
        }

        /**
         * 设置背景遮盖层的透明度
         */
        open fun setBackgroundDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float): B {
            backgroundDimAmount = dimAmount
            if (isCreated()) {
                popupWindow!!.setBackgroundDimAmount(dimAmount)
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
         * 添加销毁监听
         */
        open fun addOnDismissListener(listener: OnDismissListener): B {
            dismissListeners.add(listener)
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
                popupWindow?.findViewById<View?>(id)?.setOnClickListener(ViewClickWrapper(popupWindow, listener))
            }
            return this as B
        }

        /**
         * 创建
         */
        @Suppress("RtlHardcoded")
        open fun create(): BasePopupWindow {
            // 判断布局是否为空
            if (contentView == null) {
                throw IllegalArgumentException("are you ok?")
            }

            // 如果当前正在显示
            if (isShowing()) {
                dismiss()
            }

            // 如果当前没有设置重心，就设置一个默认的重心
            if (gravity == DEFAULT_ANCHORED_GRAVITY) {
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
            popupWindow = createPopupWindow(context)
            popupWindow!!.let { popupWindow ->
                popupWindow.contentView = contentView
                popupWindow.width = width
                popupWindow.height = height
                popupWindow.animationStyle = animStyle
                popupWindow.isFocusable = focusable
                popupWindow.isTouchable = touchable
                popupWindow.isOutsideTouchable = outsideTouchable
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.setOnShowListeners(showListeners)
                popupWindow.setOnDismissListeners(dismissListeners)
                popupWindow.setBackgroundDimAmount(backgroundDimAmount)

                clickArray?.let { array ->
                    var i = 0
                    while (i < array.size()) {
                        contentView!!.findViewById<View?>(array.keyAt(i))?.
                        setOnClickListener(ViewClickWrapper(popupWindow, array.valueAt(i)))
                        i++
                    }
                }

                // 将 PopupWindow 的生命周期和 Activity 绑定在一起
                getActivity()?.let { activity ->
                    PopupWindowLifecycle.with(activity, popupWindow)
                }
                createListener?.onCreate(popupWindow)
            }
            return popupWindow!!
        }

        /**
         * 显示为下拉
         */
        open fun showAsDropDown(anchor: View?) {
            getActivity()?.let {
                if (it.isFinishing || it.isDestroyed) {
                    return
                }
                if (!isCreated()) {
                    create()
                }
                popupWindow?.showAsDropDown(anchor, xOffset, yOffset, gravity)
            }
        }

        /**
         * 显示在指定位置
         */
        open fun showAtLocation(parent: View?) {
            getActivity()?.let {
                if (it.isFinishing || it.isDestroyed) {
                    return
                }
                if (!isCreated()) {
                    create()
                }
                popupWindow?.showAtLocation(parent, gravity, xOffset, yOffset)
            }
        }

        override fun getContext(): Context {
            return context
        }

        /**
         * 当前 PopupWindow 是否创建了
         */
        open fun isCreated(): Boolean {
            return popupWindow != null
        }

        /**
         * 当前 PopupWindow 是否显示了
         */
        open fun isShowing(): Boolean {
            return isCreated() && popupWindow!!.isShowing
        }

        /**
         * 销毁当前 PopupWindow
         */
        open fun dismiss() {
            getActivity()?.let {
                if (it.isFinishing || it.isDestroyed) {
                    return
                }
                popupWindow?.dismiss()
            }
        }

        /**
         * 创建 PopupWindow 对象（子类可以重写此方法来改变 PopupWindow 类型）
         */
        protected open fun createPopupWindow(context: Context): BasePopupWindow {
            return BasePopupWindow(context)
        }

        /**
         * 获取 PopupWindow 的根布局
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
         * 获取当前 PopupWindow 对象
         */
        open fun getPopupWindow(): BasePopupWindow? {
            return popupWindow
        }

        /**
         * 延迟执行
         */
        open fun post(runnable: Runnable) {
            if (isShowing()) {
                popupWindow!!.post(runnable)
            } else {
                addOnShowListener(ShowPostWrapper(runnable))
            }
        }

        /**
         * 延迟一段时间执行
         */
        open fun postDelayed(runnable: Runnable, delayMillis: Long) {
            if (isShowing()) {
                popupWindow!!.postDelayed(runnable, delayMillis)
            } else {
                addOnShowListener(ShowPostDelayedWrapper(runnable, delayMillis))
            }
        }

        /**
         * 在指定的时间执行
         */
        open fun postAtTime(runnable: Runnable, uptimeMillis: Long) {
            if (isShowing()) {
                popupWindow!!.postAtTime(runnable, uptimeMillis)
            } else {
                addOnShowListener(ShowPostAtTimeWrapper(runnable, uptimeMillis))
            }
        }
    }

    /**
     * PopupWindow 生命周期绑定
     */
    private class PopupWindowLifecycle constructor(private var activity: Activity?,
                                                   private var popupWindow: BasePopupWindow?) :
        ActivityLifecycleCallbacks, OnShowListener, OnDismissListener {

        companion object {
            fun with(activity: Activity, popupWindow: BasePopupWindow?) {
                PopupWindowLifecycle(activity, popupWindow)
            }
        }

        init {
            popupWindow?.addOnShowListener(this)
            popupWindow?.addOnDismissListener(this)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (this.activity !== activity) {
                return
            }
            unregisterActivityLifecycleCallbacks()
            this.activity = null
            if (this.popupWindow == null) {
                return
            }
            this.popupWindow!!.removeOnShowListener(this)
            this.popupWindow!!.removeOnDismissListener(this)
            if (this.popupWindow!!.isShowing) {
                this.popupWindow!!.dismiss()
            }
            this.popupWindow = null
        }

        override fun onShow(popupWindow: BasePopupWindow?) {
            this.popupWindow = popupWindow
            registerActivityLifecycleCallbacks()
        }

        override fun onDismiss(popupWindow: BasePopupWindow?) {
            this.popupWindow = null
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
     * PopupWindow 背景遮盖层实现类
     */
    private class PopupBackground : OnShowListener, OnDismissListener {

        private var alpha: Float = 0f
        
        fun setAlpha(alpha: Float) {
            this.alpha = alpha
        }

        override fun onShow(popupWindow: BasePopupWindow?) {
            popupWindow?.setActivityAlpha(alpha)
        }

        override fun onDismiss(popupWindow: BasePopupWindow?) {
            popupWindow?.setActivityAlpha(1f)
        }
    }

    /**
     * 销毁监听包装类
     */
    private class DismissListenerWrapper constructor(referent: PopupWindow.OnDismissListener?) :
        SoftReference<PopupWindow.OnDismissListener?>(referent), OnDismissListener {

        override fun onDismiss(popupWindow: BasePopupWindow?) {
            // 在横竖屏切换后监听对象会为空
            get()?.onDismiss()
        }
    }

    /**
     * 点击事件包装类
     */
    private class ViewClickWrapper constructor(

        private val popupWindow: BasePopupWindow?,
        private val listener: OnClickListener<View>?) : View.OnClickListener {

        override fun onClick(view: View) {
            listener?.onClick(popupWindow, view)
        }
    }

    /**
     * post 任务包装类
     */
    private class ShowPostWrapper constructor(val runnable: Runnable) : OnShowListener {

        override fun onShow(popupWindow: BasePopupWindow?) {
            popupWindow?.removeOnShowListener(this)
            popupWindow?.post(runnable)
        }
    }

    /**
     * postDelayed 任务包装类
     */
    private class ShowPostDelayedWrapper constructor(
        private val runnable: Runnable,
        private val delayMillis: Long) : OnShowListener {

        override fun onShow(popupWindow: BasePopupWindow?) {
            popupWindow?.removeOnShowListener(this)
            popupWindow?.postDelayed(runnable, delayMillis)
        }
    }

    /**
     * postAtTime 任务包装类
     */
    private class ShowPostAtTimeWrapper constructor(
        private val runnable: Runnable,
        private val uptimeMillis: Long) : OnShowListener {

        override fun onShow(popupWindow: BasePopupWindow?) {
            popupWindow?.removeOnShowListener(this)
            popupWindow?.postAtTime(runnable, uptimeMillis)
        }
    }

    /**
     * 点击监听器
     */
    interface OnClickListener<V : View> {

        /**
         * 点击事件触发了
         */
        fun onClick(popupWindow: BasePopupWindow?, view: V)
    }

    /**
     * 创建监听器
     */
    interface OnCreateListener {

        /**
         * PopupWindow 创建了
         */
        fun onCreate(popupWindow: BasePopupWindow?)
    }

    /**
     * 显示监听器
     */
    interface OnShowListener {

        /**
         * PopupWindow 显示了
         */
        fun onShow(popupWindow: BasePopupWindow?)
    }

    /**
     * 销毁监听器
     */
    interface OnDismissListener {

        /**
         * PopupWindow 销毁了
         */
        fun onDismiss(popupWindow: BasePopupWindow?)
    }
}