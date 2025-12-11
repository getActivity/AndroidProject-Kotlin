package com.hjq.demo.action

import android.graphics.drawable.Drawable
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
     * 设置标题栏的标题
     */
    fun setTitle(@StringRes id: Int) {
        val titleBar = getTitleBar() ?: return
        titleBar.setTitle(id)
    }

    /**
     * 设置标题栏的标题
     */
    fun setTitle(title: CharSequence?) {
        val titleBar = getTitleBar() ?: return
        titleBar.title = title
    }

    /**
     * 设置标题栏的左标题
     */
    fun setLeftTitle(id: Int) {
        val titleBar = getTitleBar() ?: return
        titleBar.setLeftTitle(id)
    }

    fun setLeftTitle(text: CharSequence?) {
        val titleBar = getTitleBar() ?: return
        titleBar.leftTitle = text
    }

    fun getLeftTitle(): CharSequence? {
        val titleBar = getTitleBar() ?: return ""
        return titleBar.leftTitle
    }

    /**
     * 设置标题栏的右标题
     */
    fun setRightTitle(id: Int) {
        val titleBar = getTitleBar() ?: return
        titleBar.setRightTitle(id)
    }

    fun setRightTitle(text: CharSequence?) {
        val titleBar = getTitleBar() ?: return
        titleBar.rightTitle = text
    }

    fun getRightTitle(): CharSequence? {
        val titleBar = getTitleBar() ?: return ""
        return titleBar.rightTitle
    }

    /**
     * 设置标题栏的左图标
     */
    fun setLeftIcon(id: Int) {
        val titleBar = getTitleBar() ?: return
        titleBar.setLeftIcon(id)
    }

    fun setLeftIcon(drawable: Drawable?) {
        val titleBar = getTitleBar() ?: return
        titleBar.leftIcon = drawable
    }

    fun getLeftIcon(): Drawable? {
        val titleBar = getTitleBar() ?: return null
        return titleBar.leftIcon
    }

    /**
     * 设置标题栏的右图标
     */
    fun setRightIcon(id: Int) {
        val titleBar = getTitleBar() ?: return
        titleBar.setRightIcon(id)
    }

    fun setRightIcon(drawable: Drawable?) {
        val titleBar = getTitleBar() ?: return
        titleBar.rightIcon = drawable
    }

    fun getRightIcon(): Drawable? {
        val titleBar = getTitleBar() ?: return null
        return titleBar.rightIcon
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