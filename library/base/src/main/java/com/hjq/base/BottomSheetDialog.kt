package com.hjq.base

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2020/09/12
 *    desc   : 在 BaseDialog 基础上加上 [com.google.android.material.bottomsheet.BottomSheetDialog] 特性
 */
@Suppress("LeakingThis")
open class BottomSheetDialog @JvmOverloads constructor(context: Context, themeResId: Int = R.style.BaseDialogTheme) :
    BaseDialog(context, themeResId), OnTouchListener, View.OnClickListener {

    private val bottomSheetBehavior: BottomSheetBehavior<FrameLayout> = BottomSheetBehavior(getContext(), null)
    private var cancelable: Boolean = true
    private var canceledOnTouchOutside: Boolean = true
    private var canceledOnTouchOutsideSet: Boolean = false

    init {
        bottomSheetBehavior.addBottomSheetCallback(MyBottomSheetCallback())
        bottomSheetBehavior.isHideable = cancelable
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    @Suppress("deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window: Window = window ?: return

        // 沉浸式状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // 隐藏底部导航栏
        val decorView: View = window.decorView
        val uiOptions: Int = (decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun setContentView(@LayoutRes layoutResId: Int) {
        super.setContentView(wrapContentView(layoutInflater.inflate(layoutResId, null, false)))
    }

    override fun setContentView(view: View) {
        super.setContentView(wrapContentView(view))
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        view.layoutParams = params
        super.setContentView(wrapContentView(view))
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        if (this.cancelable == cancelable) {
            return
        }
        this.cancelable = cancelable
        bottomSheetBehavior.isHideable = cancelable
    }

    override fun onStart() {
        super.onStart()
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            return
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun cancel() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            super.cancel()
            return
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        if (cancel && !cancelable) {
            cancelable = true
        }
        canceledOnTouchOutside = cancel
        canceledOnTouchOutsideSet = true
    }

    private fun shouldWindowCloseOnTouchOutside(): Boolean {
        if (!canceledOnTouchOutsideSet) {
            val array: TypedArray =
                context.obtainStyledAttributes(intArrayOf(android.R.attr.windowCloseOnTouchOutside))
            canceledOnTouchOutside = array.getBoolean(0, true)
            array.recycle()
            canceledOnTouchOutsideSet = true
        }
        return canceledOnTouchOutside
    }

    @Suppress("ClickableViewAccessibility")
    private fun wrapContentView(view: View): View {
        val rootLayout = CoordinatorLayout(context)
        rootLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val touchView = View(context)
        touchView.isSoundEffectsEnabled = false
        touchView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        touchView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val contentLayout = FrameLayout(context)
        val layoutParams: CoordinatorLayout.LayoutParams = CoordinatorLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        layoutParams.behavior = bottomSheetBehavior
        contentLayout.layoutParams = layoutParams
        contentLayout.addView(view)
        rootLayout.addView(touchView)
        rootLayout.addView(contentLayout)
        touchView.setOnClickListener(this)
        ViewCompat.setAccessibilityDelegate(contentLayout, BehaviorAccessibilityDelegate())
        contentLayout.setOnTouchListener(this)
        return rootLayout
    }

    open fun getBottomSheetBehavior(): BottomSheetBehavior<FrameLayout> {
        return bottomSheetBehavior
    }

    /**
     * [View.OnClickListener]
     */
    override fun onClick(view: View) {
        if (cancelable && isShowing && shouldWindowCloseOnTouchOutside()) {
            cancel()
        }
    }

    /**
     * [OnTouchListener]
     */
    @Suppress("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        return true
    }

    private inner class MyBottomSheetCallback : BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                cancel()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private inner class BehaviorAccessibilityDelegate : AccessibilityDelegateCompat() {

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            if (cancelable) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS)
                info.isDismissable = true
            } else {
                info.isDismissable = false
            }
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
            if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && cancelable) {
                cancel()
                return true
            }
            return super.performAccessibilityAction(host, action, args)
        }
    }
}