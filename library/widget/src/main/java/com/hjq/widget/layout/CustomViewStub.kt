package com.hjq.widget.layout

import android.content.*
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.hjq.widget.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/07/06
 *    desc   : 自定义 ViewStub（原生 ViewStub 的缺点：继承至 View，不支持 findViewById、动态添加和移除 View、监听显示隐藏）
 */
class CustomViewStub @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var listener: OnViewStubListener? = null
    private val layoutResource: Int
    private var inflateView: View? = null

    init {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewStub)
        layoutResource = array.getResourceId(R.styleable.CustomViewStub_android_layout, 0)
        array.recycle()

        // 隐藏自己
        visibility = GONE
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (inflateView == null && visibility != GONE) {
            inflateView = LayoutInflater.from(context).inflate(layoutResource, this, false)
            val layoutParams: LayoutParams? = inflateView!!.layoutParams as LayoutParams?
            if (layoutParams != null) {
                layoutParams.width = getLayoutParams().width
                layoutParams.height = getLayoutParams().height
                if (layoutParams.gravity == LayoutParams.UNSPECIFIED_GRAVITY) {
                    layoutParams.gravity = Gravity.CENTER
                }
                inflateView!!.layoutParams = layoutParams
            }
            addView(inflateView)
            listener?.onInflate(this, inflateView!!)
        }
        listener?.onVisibility(this, visibility)
    }

    /**
     * 设置显示状态（避免 setVisibility 导致的无限递归）
     */
    fun setCustomVisibility(visibility: Int) {
        super.setVisibility(visibility)
    }

    /**
     * 获取填充的 View
     */
    fun getInflateView(): View? {
        return inflateView
    }

    /**
     * 设置监听器
     */
    fun setOnViewStubListener(listener: OnViewStubListener?) {
        this.listener = listener
    }

    interface OnViewStubListener {

        /**
         * 布局填充回调（可在此中做 View 初始化）
         *
         * @param stub              当前 ViewStub 对象
         * @param inflatedView      填充布局对象
         */
        fun onInflate(stub: CustomViewStub, inflatedView: View)

        /**
         * 可见状态改变（可在此中做 View 更新）
         *
         * @param stub              当前 ViewStub 对象
         * @param visibility        可见状态参数改变
         */
        fun onVisibility(stub: CustomViewStub, visibility: Int)
    }
}