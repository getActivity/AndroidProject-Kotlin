package com.hjq.demo.ui.dialog.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.core.recycler.PickerLayoutManager
import com.hjq.core.recycler.PickerLayoutManager.OnPickerListener
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/17
 *    desc   : 日期选择对话框
 */
class DateDialog {
    
    class Builder @JvmOverloads constructor(
        context: Context, private val
        startYear: Int = Calendar.getInstance(Locale.CHINA)[Calendar.YEAR] - 100,
        endYear: Int = Calendar.getInstance(Locale.CHINA)[Calendar.YEAR]
    ) : StyleDialog.Builder<Builder>(context), Runnable, OnPickerListener {

        private val yearView: RecyclerView? by lazyFindViewById(R.id.rv_date_year)
        private val monthView: RecyclerView? by lazyFindViewById(R.id.rv_date_month)
        private val dayView: RecyclerView? by lazyFindViewById(R.id.rv_date_day)

        private val yearManager: PickerLayoutManager
        private val monthManager: PickerLayoutManager
        private val dayManager: PickerLayoutManager
        private val yearAdapter: PickerAdapter
        private val monthAdapter: PickerAdapter
        private val dayAdapter: PickerAdapter

        private var listener: OnListener? = null

        init {
            setCustomView(R.layout.date_dialog)
            setTitle(R.string.time_title)
            yearAdapter = PickerAdapter(context)
            monthAdapter = PickerAdapter(context)
            dayAdapter = PickerAdapter(context)

            // 生产年份
            val yearData: MutableList<String> = mutableListOf()
            for (i in startYear..endYear) {
                yearData.add(i.toString() + " " + getString(R.string.common_year))
            }

            // 生产月份
            val monthData: MutableList<String> = mutableListOf()
            for (i in 1..12) {
                monthData.add(i.toString() + " " + getString(R.string.common_month))
            }
            val calendar = Calendar.getInstance(Locale.CHINA)
            val day = calendar.getActualMaximum(Calendar.DATE)
            // 生产天数
            val dayData: MutableList<String> = mutableListOf()
            for (i in 1..day) {
                dayData.add(i.toString() + " " + getString(R.string.common_day))
            }
            yearAdapter.setData(yearData)
            monthAdapter.setData(monthData)
            dayAdapter.setData(dayData)
            yearManager = PickerLayoutManager.Builder(context)
                .build()
            monthManager = PickerLayoutManager.Builder(context)
                .build()
            dayManager = PickerLayoutManager.Builder(context)
                .build()
            yearView?.layoutManager = yearManager
            monthView?.layoutManager = monthManager
            dayView?.layoutManager = dayManager
            yearView?.adapter = yearAdapter
            monthView?.adapter = monthAdapter
            dayView?.adapter = dayAdapter
            setYear(calendar[Calendar.YEAR])
            setMonth(calendar[Calendar.MONTH] + 1)
            setDay(calendar[Calendar.DAY_OF_MONTH])
            yearManager.setOnPickerListener(this)
            monthManager.setOnPickerListener(this)
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        /**
         * 不选择天数
         */
        fun setIgnoreDay(): Builder = apply {
            dayView?.visibility = View.GONE
        }

        fun setDate(date: Long): Builder = apply {
            if (date > 0) {
                setDate(SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(date)))
            }
        }

        fun setDate(date: String): Builder = apply {
            if (date.matches(Regex("\\d{8}"))) {
                // 20190519
                setYear(date.substring(0, 4))
                setMonth(date.substring(4, 6))
                setDay(date.substring(6, 8))
            } else if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                // 2019-05-19
                setYear(date.substring(0, 4))
                setMonth(date.substring(5, 7))
                setDay(date.substring(8, 10))
            }
        }

        fun setYear(year: String): Builder = apply {
            return setYear(year.toInt())
        }

        fun setYear(year: Int): Builder = apply {
            var index = year - startYear
            if (index < 0) {
                index = 0
            } else if (index > yearAdapter.getCount() - 1) {
                index = yearAdapter.getCount() - 1
            }
            yearView?.scrollToPosition(index)
            postRefreshMonthMaximumDay()
        }

        fun setMonth(month: String): Builder = apply {
            setMonth(month.toInt())
        }

        fun setMonth(month: Int): Builder = apply {
            var index = month - 1
            if (index < 0) {
                index = 0
            } else if (index > monthAdapter.getCount() - 1) {
                index = monthAdapter.getCount() - 1
            }
            monthView?.scrollToPosition(index)
            postRefreshMonthMaximumDay()
        }

        fun setDay(day: String): Builder = apply {
            setDay(day.toInt())
        }

        fun setDay(day: Int): Builder = apply {
            refreshMonthMaximumDay()
            var index = day - 1
            if (index < 0) {
                index = 0
            } else if (index > dayAdapter.getCount() - 1) {
                index = dayAdapter.getCount() - 1
            }
            dayView?.scrollToPosition(index)
        }

        @SingleClick
        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_ui_confirm -> {
                    performClickDismiss()
                    listener?.onSelected(requireNotNull(getDialog()), startYear + yearManager.getPickedPosition(),
                        monthManager.getPickedPosition() + 1, dayManager.getPickedPosition() + 1)
                }
                R.id.tv_ui_cancel -> {
                    performClickDismiss()
                    listener?.onCancel(requireNotNull(getDialog()))
                }
            }
        }

        /**
         * [PickerLayoutManager.OnPickerListener]
         *
         * @param recyclerView              RecyclerView 对象
         * @param position                  当前滚动的位置
         */
        override fun onPicked(recyclerView: RecyclerView, position: Int) {
            postRefreshMonthMaximumDay()
        }

        override fun run() {
            refreshMonthMaximumDay()
        }

        /**
         * 刷新每个月天最大天数
         */
        private fun refreshMonthMaximumDay() {
            // 获取这个月最多有多少天
            val calendar = Calendar.getInstance(Locale.CHINA)
            calendar[startYear + yearManager.getPickedPosition(), monthManager.getPickedPosition()] = 1

            val day = calendar.getActualMaximum(Calendar.DATE)
            if (dayAdapter.getCount() == day) {
                return
            }

            val dayData: MutableList<String> = mutableListOf()
            for (i in 1..day) {
                dayData.add(i.toString() + " " + getString(R.string.common_day))
            }
            dayAdapter.setData(dayData)
        }

        private fun postRefreshMonthMaximumDay() {
            yearView?.removeCallbacks(this)
            yearView?.post(this)
        }

        class PickerAdapter(context: Context) : AppAdapter<String>(context) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
                return ViewHolder()
            }

            inner class ViewHolder : AppViewHolder(R.layout.picker_item) {

                private val pickerView: TextView? by lazyFindViewById(R.id.tv_picker_name)

                override fun onBindView(position: Int) {
                    pickerView?.text = getItem(position)
                }
            }
        }
    }

    interface OnListener {

        /**
         * 选择完日期后回调
         *
         * @param year              年
         * @param month             月
         * @param day               日
         */
        fun onSelected(dialog: BaseDialog, year: Int, month: Int, day: Int)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog) {
            // default implementation ignored
        }
    }
}