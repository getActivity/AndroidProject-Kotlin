package com.hjq.demo.other

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.hjq.bar.style.LightBarStyle
import com.hjq.base.ktx.dp2px
import com.hjq.base.ktx.sp2px
import com.hjq.demo.R
import com.hjq.widget.view.PressAlphaTextView

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2021/02/27
 *    desc   : 标题栏初始器
 */
class TitleBarStyle : LightBarStyle() {

    override fun newTitleView(context: Context): TextView {
        return AppCompatTextView(context)
    }

    override fun newLeftView(context: Context): TextView {
        return PressAlphaTextView(context)
    }

    override fun newRightView(context: Context): TextView {
        return PressAlphaTextView(context)
    }

    override fun getTitleBarBackground(context: Context): Drawable {
        return ColorDrawable(ContextCompat.getColor(context, R.color.common_primary_color))
    }

    override fun getBackButtonDrawable(context: Context): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.arrows_left_ic)
    }

    override fun getLeftTitleBackground(context: Context): Drawable? {
        return null
    }

    override fun getRightTitleBackground(context: Context): Drawable? {
        return null
    }

    override fun getTitleHorizontalPadding(context: Context): Int {
        return 0
    }

    override fun getLeftHorizontalPadding(context: Context): Int {
        return context.dp2px(10).toInt()
    }

    override fun getRightHorizontalPadding(context: Context): Int {
        return context.dp2px(10).toInt()
    }

    override fun getChildVerticalPadding(context: Context): Int {
        return context.dp2px(14).toInt()
    }

    override fun getTitleSize(context: Context): Float {
        return context.sp2px(15)
    }

    override fun getLeftTitleSize(context: Context): Float {
        return context.sp2px(13)
    }

    override fun getRightTitleSize(context: Context): Float {
        return context.sp2px(13)
    }

    override fun getTitleIconPadding(context: Context): Int {
        return context.dp2px(2).toInt()
    }

    override fun getLeftIconPadding(context: Context): Int {
        return context.dp2px(2).toInt()
    }

    override fun getRightIconPadding(context: Context): Int {
        return context.dp2px(2).toInt()
    }
}