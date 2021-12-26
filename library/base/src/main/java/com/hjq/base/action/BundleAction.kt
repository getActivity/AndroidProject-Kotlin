package com.hjq.base.action

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/10/23
 *    desc   : 参数意图
 */
interface BundleAction {

    fun getBundle(): Bundle?

    fun getInt(name: String): Int {
        return getInt(name, 0)
    }

    fun getInt(name: String, defaultValue: Int): Int {
        val bundle: Bundle = getBundle() ?: return defaultValue
        return bundle.getInt(name, defaultValue)
    }

    fun getLong(name: String): Long {
        return getLong(name, 0)
    }

    fun getLong(name: String, defaultValue: Long): Long {
        val bundle: Bundle = getBundle() ?: return defaultValue
        return bundle.getLong(name, defaultValue)
    }

    fun getFloat(name: String): Float {
        return getFloat(name, 0f)
    }

    fun getFloat(name: String, defaultValue: Float): Float {
        val bundle: Bundle = getBundle() ?: return defaultValue
        return bundle.getFloat(name, defaultValue)
    }

    fun getDouble(name: String): Double {
        return getDouble(name, 0.0)
    }

    fun getDouble(name: String, defaultValue: Double): Double {
        val bundle: Bundle = getBundle() ?: return defaultValue
        return bundle.getDouble(name, defaultValue)
    }

    fun getBoolean(name: String): Boolean {
        return getBoolean(name, false)
    }

    fun getBoolean(name: String, defaultValue: Boolean): Boolean {
        val bundle: Bundle = getBundle() ?: return defaultValue
        return bundle.getBoolean(name, defaultValue)
    }

    fun getString(name: String): String? {
        val bundle: Bundle = getBundle() ?: return null
        return bundle.getString(name)
    }

    fun <P : Parcelable?> getParcelable(name: String): P? {
        val bundle: Bundle = getBundle() ?: return null
        return bundle.getParcelable(name)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : Serializable?> getSerializable(name: String): S? {
        val bundle: Bundle = getBundle() ?: return null
        return (bundle.getSerializable(name)) as S?
    }

    fun getStringArrayList(name: String): ArrayList<String?>? {
        val bundle: Bundle = getBundle() ?: return null
        return bundle.getStringArrayList(name)
    }

    fun getIntegerArrayList(name: String): ArrayList<Int?>? {
        val bundle: Bundle = getBundle() ?: return null
        return bundle.getIntegerArrayList(name)
    }
}