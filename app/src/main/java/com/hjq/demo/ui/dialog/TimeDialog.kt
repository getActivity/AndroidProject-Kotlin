package com.hjq.demo.ui.dialog

import android.content.*
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.manager.PickerLayoutManager
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/08/17
 *    desc   : 时间选择对话框
 */
class TimeDialog {

    class Builder(context: Context) : CommonDialog.Builder<Builder>(context) {

        private val hourView: RecyclerView? by lazy { findViewById(R.id.rv_time_hour) }
        private val minuteView: RecyclerView? by lazy { findViewById(R.id.rv_time_minute) }
        private val secondView: RecyclerView? by lazy { findViewById(R.id.rv_time_second) }

        private val hourManager: PickerLayoutManager
        private val minuteManager: PickerLayoutManager
        private val secondManager: PickerLayoutManager

        private val hourAdapter: PickerAdapter
        private val minuteAdapter: PickerAdapter
        private val secondAdapter: PickerAdapter

        private var listener: OnListener? = null

        init {
            setCustomView(R.layout.time_dialog)
            setTitle(R.string.time_title)
            hourAdapter = PickerAdapter(context)
            minuteAdapter = PickerAdapter(context)
            secondAdapter = PickerAdapter(context)

            // 生产小时
            val hourData = ArrayList<String?>(24)
            for (i in 0..23) {
                hourData.add((if (i < 10) "0" else "") + i + " " + getString(R.string.common_hour))
            }

            // 生产分钟
            val minuteData = ArrayList<String?>(60)
            for (i in 0..59) {
                minuteData.add((if (i < 10) "0" else "") + i + " " + getString(R.string.common_minute))
            }

            // 生产秒钟
            val secondData = ArrayList<String?>(60)
            for (i in 0..59) {
                secondData.add((if (i < 10) "0" else "") + i + " " + getString(R.string.common_second))
            }

            hourAdapter.setData(hourData)
            minuteAdapter.setData(minuteData)
            secondAdapter.setData(secondData)

            hourManager = PickerLayoutManager.Builder(context).build()
            minuteManager = PickerLayoutManager.Builder(context).build()
            secondManager = PickerLayoutManager.Builder(context).build()

            hourView?.layoutManager = hourManager
            minuteView?.layoutManager = minuteManager
            secondView?.layoutManager = secondManager

            hourView?.adapter = hourAdapter
            minuteView?.adapter = minuteAdapter
            secondView?.adapter = secondAdapter

            val calendar = Calendar.getInstance()
            setHour(calendar[Calendar.HOUR_OF_DAY])
            setMinute(calendar[Calendar.MINUTE])
            setSecond(calendar[Calendar.SECOND])
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        /**
         * 不选择秒数
         */
        fun setIgnoreSecond(): Builder = apply {
            secondView?.visibility = View.GONE
        }

        fun setTime(time: String?): Builder = apply {
            if (time == null) {
                return@apply
            }
            // 102030
            if (time.matches(Regex("\\d{6}"))) {
                setHour(time.substring(0, 2))
                setMinute(time.substring(2, 4))
                setSecond(time.substring(4, 6))
                // 10:20:30
            } else if (time.matches(Regex("\\d{2}:\\d{2}:\\d{2}"))) {
                setHour(time.substring(0, 2))
                setMinute(time.substring(3, 5))
                setSecond(time.substring(6, 8))
            }
        }

        fun setHour(hour: String?): Builder = apply {
            if (hour == null) {
                return@apply
            }
            setHour(hour.toInt())
        }

        fun setHour(hour: Int): Builder = apply {
            var index = hour
            if (index < 0 || hour == 24) {
                index = 0
            } else if (index > hourAdapter.getCount() - 1) {
                index = hourAdapter.getCount() - 1
            }
            hourView?.scrollToPosition(index)
        }

        fun setMinute(minute: String?): Builder = apply {
            if (minute == null) {
                return@apply
            }
            setMinute(minute.toInt())
        }

        fun setMinute(minute: Int): Builder = apply {
            var index = minute
            if (index < 0) {
                index = 0
            } else if (index > minuteAdapter.getCount() - 1) {
                index = minuteAdapter.getCount() - 1
            }
            minuteView?.scrollToPosition(index)
        }

        fun setSecond(second: String?): Builder = apply {
            if (second == null) {
                return@apply
            }
            setSecond(second.toInt())
        }

        fun setSecond(second: Int): Builder = apply {
            var index = second
            if (index < 0) {
                index = 0
            } else if (index > secondAdapter.getCount() - 1) {
                index = secondAdapter.getCount() - 1
            }
            secondView?.scrollToPosition(index)
        }

        @SingleClick
        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_ui_confirm -> {
                    autoDismiss()
                    listener?.onSelected(getDialog(), hourManager.getPickedPosition(),
                        minuteManager.getPickedPosition(), secondManager.getPickedPosition())
                }
                R.id.tv_ui_cancel -> {
                    autoDismiss()
                    listener?.onCancel(getDialog())
                }
            }
        }
    }

    private class PickerAdapter(context: Context) : AppAdapter<String?>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder()
        }

        private inner class ViewHolder : AppViewHolder(R.layout.picker_item) {

            private val pickerView: TextView? by lazy { findViewById(R.id.tv_picker_name) }

            override fun onBindView(position: Int) {
                pickerView?.text = getItem(position)
            }
        }
    }

    interface OnListener {

        /**
         * 选择完时间后回调
         *
         * @param hour              时钟
         * @param minute            分钟
         * @param second            秒钟
         */
        fun onSelected(dialog: BaseDialog?, hour: Int, minute: Int, second: Int)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog?) {}
    }
}