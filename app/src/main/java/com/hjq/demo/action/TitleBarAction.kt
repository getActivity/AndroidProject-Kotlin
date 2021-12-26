package com.hjq.demo.action

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/12/08
 *    desc   : 标题栏意图
 */
interface TitleBarAction : OnTitleBarListener {

    /**
     * 获取标题栏对象
     */
    fun getTitleBar(): TitleBar?

    /**
     * 左项被点击
     *
     * @param view     被点击的左项View
     */
    override fun onLeftClick(view: View) {}

    /**
     * 标题被点击
     *
     * @param view     被点击的标题View
     */
    override fun onTitleClick(view: View) {}

    /**
     * 右项被点击
     *
     * @param view     被点击的右项View
     */
    override fun onRightClick(view: View) {}

    /**
     * 设置标题栏的标题
     */
    fun setTitle(@StringRes id: Int) {
        getTitleBar()?.setTitle(id)
    }

    /**
     * 设置标题栏的标题
     */
    fun setTitle(title: CharSequence?) {
        getTitleBar()?.title = title
    }

    /**
     * 设置标题栏的左标题
     */
    fun setLeftTitle(id: Int) {
        getTitleBar()?.setLeftTitle(id)
    }

    fun setLeftTitle(text: CharSequence?) {
        getTitleBar()?.leftTitle = text
    }

    fun getLeftTitle(): CharSequence? {
        return getTitleBar()?.leftTitle
    }

    /**
     * 设置标题栏的右标题
     */
    fun setRightTitle(id: Int) {
        getTitleBar()?.setRightTitle(id)
    }

    fun setRightTitle(text: CharSequence?) {
        getTitleBar()?.rightTitle = text
    }

    fun getRightTitle(): CharSequence? {
        return getTitleBar()?.rightTitle
    }

    /**
     * 设置标题栏的左图标
     */
    fun setLeftIcon(id: Int) {
        getTitleBar()?.setLeftIcon(id)
    }

    fun setLeftIcon(drawable: Drawable?) {
        getTitleBar()?.leftIcon = drawable
    }

    fun getLeftIcon(): Drawable? {
        return getTitleBar()?.leftIcon
    }

    /**
     * 设置标题栏的右图标
     */
    fun setRightIcon(id: Int) {
        getTitleBar()?.setRightIcon(id)
    }

    fun setRightIcon(drawable: Drawable?) {
        getTitleBar()?.rightIcon = drawable
    }

    fun getRightIcon(): Drawable? {
        return getTitleBar()?.rightIcon
    }

    /**
     * 递归获取 ViewGroup 中的 TitleBar 对象
     */
    fun obtainTitleBar(group: ViewGroup?): TitleBar? {
        if (group == null) {
            return null
        }
        for (i in 0 until group.childCount) {
            val view = group.getChildAt(i)
            if (view is TitleBar) {
                return view
            }
            if (view is ViewGroup) {
                val titleBar = obtainTitleBar(view)
                if (titleBar != null) {
                    return titleBar
                }
            }
        }
        return null
    }
}