package com.hjq.base.ktx

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import com.hjq.base.BaseAdapter
import com.hjq.base.BaseDialog
import com.hjq.base.BaseFragment
import com.hjq.base.BasePopupWindow

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2022/03/10
 *    desc   : findViewById 懒加载委托扩展
 */
fun <V : View?> View.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> Activity.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> BaseFragment<*>.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> BaseAdapter<*>.BaseViewHolder.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> BaseDialog.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> BaseDialog.Builder<*>.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> BasePopupWindow.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> BasePopupWindow.Builder<*>.lazyFindViewById(@IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    return@lazy findViewById<V>(id)
}

fun <V : View?> Any.lazyFindViewById(view: View?, @IdRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    if (view == null) {
        return@lazy null
    }
    return@lazy view.findViewById<V>(id)
}