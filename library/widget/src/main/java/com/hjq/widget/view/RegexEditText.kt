package com.hjq.widget.view

import android.content.*
import android.content.res.TypedArray
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.hjq.widget.R
import java.util.regex.Pattern

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/06/29
 *    desc   : 正则输入限制编辑框
 */
open class RegexEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr), InputFilter {

    companion object {

        /** 手机号（只能以 1 开头） */
        const val REGEX_MOBILE: String = "[1]\\d{0,10}"

        /** 中文（普通的中文字符） */
        const val REGEX_CHINESE: String = "[\\u4e00-\\u9fa5]*"

        /** 英文（大写和小写的英文）*/
        const val REGEX_ENGLISH: String = "[a-zA-Z]*"

        /** 数字（只允许输入纯数字）*/
        const val REGEX_NUMBER: String = "\\d*"

        /** 计数（非 0 开头的数字）*/
        const val REGEX_COUNT: String = "[1-9]\\d*"

        /** 用户名（中文、英文、数字）*/
        const val REGEX_NAME: String = "[[\\u4e00-\\u9fa5]|[a-zA-Z]|\\d]*"

        /** 非空格的字符（不能输入空格）*/
        const val REGEX_NONNULL: String = "\\S+"
    }

    /** 正则表达式规则 */
    private var pattern: Pattern? = null

    init {
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.RegexEditText)
        if (array.hasValue(R.styleable.RegexEditText_inputRegex)) {
            setInputRegex(array.getString(R.styleable.RegexEditText_inputRegex))
        } else if (array.hasValue(R.styleable.RegexEditText_regexType)) {
            when (array.getInt(R.styleable.RegexEditText_regexType, 0)) {
                0x01 -> setInputRegex(REGEX_MOBILE)
                0x02 -> setInputRegex(REGEX_CHINESE)
                0x03 -> setInputRegex(REGEX_ENGLISH)
                0x04 -> setInputRegex(REGEX_NUMBER)
                0x05 -> setInputRegex(REGEX_COUNT)
                0x06 -> setInputRegex(REGEX_NAME)
                0x07 -> setInputRegex(REGEX_NONNULL)
            }
        }
        array.recycle()
    }

    /**
     * 是否有这个输入标记
     */
    fun hasInputType(type: Int): Boolean {
        return (inputType and type) != 0
    }

    /**
     * 添加一个输入标记
     */
    fun addInputType(type: Int) {
        inputType = inputType or type
    }

    /**
     * 移除一个输入标记
     */
    fun removeInputType(type: Int) {
        inputType = inputType and type.inv()
    }

    /**
     * 设置输入正则
     */
    fun setInputRegex(regex: String?) {
        if (TextUtils.isEmpty(regex)) {
            return
        }
        pattern = Pattern.compile(regex!!)
        addFilters(this)
    }

    /**
     * 获取输入正则
     */
    fun getInputRegex(): String? {
        if (pattern == null) {
            return null
        }
        return pattern!!.pattern()
    }

    /**
     * 添加筛选规则
     */
    fun addFilters(filter: InputFilter?) {
        if (filter == null) {
            return
        }
        val newFilters: Array<InputFilter?>?
        val oldFilters: Array<InputFilter?>? = filters
        if (oldFilters != null && oldFilters.isNotEmpty()) {
            newFilters = arrayOfNulls<InputFilter?>(oldFilters.size + 1)
            // 复制旧数组的元素到新数组中
            System.arraycopy(oldFilters, 0, newFilters, 0, oldFilters.size)
            newFilters[oldFilters.size] = filter
        } else {
            newFilters = arrayOfNulls<InputFilter?>(1)
            newFilters[0] = filter
        }
        super.setFilters(newFilters)
    }

    /**
     * 清空筛选规则
     */
    fun clearFilters() {
        super.setFilters(arrayOfNulls(0))
    }

    /**
     * [InputFilter]
     *
     * @param source        新输入的字符串
     * @param start         新输入的字符串起始下标
     * @param end           新输入的字符串终点下标
     * @param dest          输入之前文本框内容
     * @param destStart     在原内容上的起始坐标
     * @param destEnd       在原内容上的终点坐标
     * @return              返回字符串将会加入到内容中
     */
    override fun filter(source: CharSequence?, start: Int, end: Int,
        dest: Spanned?, destStart: Int, destEnd: Int): CharSequence? {

        if (pattern == null) {
            return source
        }

        // 拼接出最终的字符串
        val begin: String = dest.toString().substring(0, destStart)
        val over: String = dest.toString().substring(
            destStart + (destEnd - destStart),
            destStart + (dest.toString().length - begin.length)
        )
        val result: String = begin + source + over

        // 判断是插入还是删除
        if (destStart > destEnd - 1) {
            // 如果是插入字符
            if (!pattern!!.matcher(result).matches()) {
                // 如果不匹配就不让这个字符输入
                return ""
            }
        } else {
            // 如果是删除字符
            if (!pattern!!.matcher(result).matches()) {
                // 如果不匹配则不让删除（删空操作除外）
                if ("" != result) {
                    return dest.toString().substring(destStart, destEnd)
                }
            }
        }

        // 不做任何修改
        return source
    }
}