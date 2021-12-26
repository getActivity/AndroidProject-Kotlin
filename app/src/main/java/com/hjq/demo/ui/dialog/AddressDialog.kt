package com.hjq.demo.ui.dialog

import android.content.*
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppAdapter
import com.hjq.demo.ui.adapter.*
import com.hjq.demo.ui.adapter.TabAdapter.OnTabListener
import com.hjq.demo.ui.dialog.AddressDialog.RecyclerViewAdapter.OnSelectListener
import com.tencent.bugly.crashreport.CrashReport
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/02/12
 *    desc   : 省市区选择对话框
 *    doc    : https://baijiahao.baidu.com/s?id=1615894776741007967
 */
class AddressDialog {

    class Builder(context: Context) : BaseDialog.Builder<Builder>(context), OnTabListener,
        Runnable, OnSelectListener, BaseDialog.OnShowListener, BaseDialog.OnDismissListener {

        private val titleView: TextView? by lazy { findViewById(R.id.tv_address_title) }
        private val closeView: ImageView? by lazy { findViewById(R.id.iv_address_close) }
        private val tabView: RecyclerView? by lazy { findViewById(R.id.rv_address_tab) }
        private val viewPager2: ViewPager2? by lazy { findViewById(R.id.vp_address_pager) }

        private val tabAdapter: TabAdapter
        private val adapter: RecyclerViewAdapter
        private val callback: OnPageChangeCallback?
        private var listener: OnListener? = null
        private var province: String = ""
        private var city: String = ""
        private var area: String = ""
        private var ignoreArea = false

        init {
            setContentView(R.layout.address_dialog)
            setHeight(getResources().displayMetrics.heightPixels / 2)
            adapter = RecyclerViewAdapter(context)
            adapter.setOnSelectListener(this)
            viewPager2?.adapter = adapter
            setOnClickListener(closeView)
            tabAdapter = TabAdapter(context, TabAdapter.TAB_MODE_SLIDING, false)
            tabAdapter.addItem(getString(R.string.address_hint))
            tabAdapter.setOnTabListener(this)
            tabView?.adapter = tabAdapter
            callback = object : OnPageChangeCallback() {

                private var mPreviousScrollState = 0
                private var mScrollState = ViewPager.SCROLL_STATE_IDLE

                override fun onPageScrollStateChanged(state: Int) {
                    mPreviousScrollState = mScrollState
                    mScrollState = state
                    viewPager2?.let {
                        if (state == ViewPager2.SCROLL_STATE_IDLE && tabAdapter.getSelectedPosition() != it.currentItem) {
                            onTabSelected(tabView, it.currentItem)
                        }
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            }

            // 显示省份列表
            adapter.addItem(AddressManager.getProvinceList(getContext()))
            addOnShowListener(this)
            addOnDismissListener(this)
        }

        fun setTitle(@StringRes id: Int): Builder = apply {
            setTitle(getString(id))
        }

        fun setTitle(text: CharSequence?): Builder = apply {
            titleView?.text = text
        }

        /**
         * 设置默认省份
         */
        fun setProvince(province: String?): Builder = apply {
            if (TextUtils.isEmpty(province)) {
                return@apply
            }
            val data = adapter.getItem(0)
            if (data == null || data.isEmpty()) {
                return@apply
            }
            for (i in data.indices) {
                if (province != data[i]!!.getName()) {
                    continue
                }
                selectedAddress(0, i, false)
                break
            }
        }

        /**
         * 设置默认城市
         */
        fun setCity(city: String?): Builder = apply {
            check(!ignoreArea) {
                // 已经忽略了县级区域的选择，不能选定指定的城市
                "The selection of county-level regions has been ignored. The designated city cannot be selected"
            }
            if (TextUtils.isEmpty(city)) {
                return@apply
            }
            val data = adapter.getItem(1)
            if (data == null || data.isEmpty()) {
                return@apply
            }
            for (i in data.indices) {
                if (city != data[i]!!.getName()) {
                    continue
                }
                // 避开直辖市，因为选择省的时候已经自动跳过市区了
                if (adapter.getItem(1)!!.size > 1) {
                    selectedAddress(1, i, false)
                }
                break
            }
        }

        /**
         * 不选择县级区域
         */
        fun setIgnoreArea(): Builder = apply {
            if (adapter.getCount() == 3) {
                // 已经指定了城市，则不能忽略县级区域
                throw IllegalStateException("Cities have been designated and county-level areas can no longer be ignored")
            }
            ignoreArea = true
        }

        fun setListener(listener: OnListener?): Builder = apply {
            this.listener = listener
        }

        /**
         * [RecyclerViewAdapter.OnSelectListener]
         */
        override fun onSelected(recyclerViewPosition: Int, clickItemPosition: Int) {
            selectedAddress(recyclerViewPosition, clickItemPosition, true)
        }

        /**
         * 选择地区
         *
         * @param type              类型（省、市、区）
         * @param position          点击的位置
         * @param smoothScroll      是否需要平滑滚动
         */
        private fun selectedAddress(type: Int, position: Int, smoothScroll: Boolean) {
            when (type) {
                0 -> {
                    // 记录当前选择的省份
                    province = adapter.getItem(type)!![position]!!.getName()!!
                    tabAdapter.setItem(type, province)
                    tabAdapter.addItem(getString(R.string.address_hint))
                    tabAdapter.setSelectedPosition(type + 1)
                    adapter.addItem(AddressManager.getCityList(adapter.getItem(type)!![position]!!.getNext()!!))
                    viewPager2?.setCurrentItem(type + 1, smoothScroll)

                    // 如果当前选择的是直辖市，就直接跳过选择城市，直接选择区域
                    if (adapter.getItem(type + 1)!!.size == 1) {
                        selectedAddress(type + 1, 0, false)
                    }
                }
                1 -> {
                    // 记录当前选择的城市
                    city = adapter.getItem(type)!![position]!!.getName()!!
                    tabAdapter.setItem(type, city)
                    if (ignoreArea) {
                        listener?.onSelected(getDialog(), province, city, area)
                        // 延迟关闭
                        postDelayed({ dismiss() }, 300)
                    } else {
                        tabAdapter.addItem(getString(R.string.address_hint))
                        tabAdapter.setSelectedPosition(type + 1)
                        adapter.addItem(
                            AddressManager.getAreaList(
                                adapter.getItem(type)!![position]!!.getNext()!!
                            )
                        )
                        viewPager2?.setCurrentItem(type + 1, smoothScroll)
                    }
                }
                2 -> {
                    // 记录当前选择的区域
                    area = adapter.getItem(type)!![position]!!.getName()!!
                    tabAdapter.setItem(type, area)
                    listener?.onSelected(getDialog(), province, city, area)

                    // 延迟关闭
                    postDelayed({ dismiss() }, 300)
                }
            }
        }

        override fun run() {
            if (isShowing()) {
                dismiss()
            }
        }

        @SingleClick
        override fun onClick(view: View) {
            if (view === closeView) {
                dismiss()
                listener?.onCancel(getDialog())
            }
        }

        /**
         * [TabAdapter.OnTabListener]
         */
        override fun onTabSelected(recyclerView: RecyclerView?, position: Int): Boolean {
            synchronized(this) {
                if (viewPager2?.currentItem != position) {
                    viewPager2?.currentItem = position
                }
                tabAdapter.setItem(position, getString(R.string.address_hint))
                when (position) {
                    0 -> {
                        province = ""
                        city = ""
                        area = ""
                        if (tabAdapter.getCount() > 2) {
                            tabAdapter.removeItem(2)
                            adapter.removeItem(2)
                        }
                        if (tabAdapter.getCount() > 1) {
                            tabAdapter.removeItem(1)
                            adapter.removeItem(1)
                        }
                    }
                    1 -> {
                        run {
                            area = ""
                            city = area
                        }
                        if (tabAdapter.getCount() > 2) {
                            tabAdapter.removeItem(2)
                            adapter.removeItem(2)
                        }
                    }
                    2 -> area = ""
                }
            }
            return true
        }

        /**
         * [BaseDialog.OnShowListener]
         */
        override fun onShow(dialog: BaseDialog?) {
            // 注册 ViewPager 滑动监听
            callback?.let { viewPager2?.registerOnPageChangeCallback(it) }
        }

        /**
         * [BaseDialog.OnDismissListener]
         */
        override fun onDismiss(dialog: BaseDialog?) {
            // 反注册 ViewPager 滑动监听
            callback?.let { viewPager2?.unregisterOnPageChangeCallback(it) }
        }
    }

    private class RecyclerViewAdapter(context: Context) :
        AppAdapter<MutableList<AddressBean?>?>(context) {

        private var listener: OnSelectListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder()
        }

        inner class ViewHolder : AppViewHolder (RecyclerView(getContext())), OnItemClickListener {

            private val adapter: AddressAdapter

            init {
                val recyclerView = getItemView() as RecyclerView
                recyclerView.isNestedScrollingEnabled = true
                recyclerView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                adapter = AddressAdapter(getContext())
                adapter.setOnItemClickListener(this)
                recyclerView.adapter = adapter
            }

            override fun onBindView(position: Int) {
                adapter.setData(getItem(position))
            }

            override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
                listener?.onSelected(getViewHolderPosition(), position)
            }
        }

        fun setOnSelectListener(listener: OnSelectListener?) {
            this.listener = listener
        }

        interface OnSelectListener {

            fun onSelected(recyclerViewPosition: Int, clickItemPosition: Int)
        }
    }

    class AddressAdapter constructor(context: Context) : AppAdapter<AddressBean?>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val textView = TextView(parent.context)
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setBackgroundResource(R.drawable.transparent_selector)
            textView.setTextColor(Color.parseColor("#222222"))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.sp_14))
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textView.setPadding(getResources().getDimension(R.dimen.dp_20).toInt(), getResources().getDimension(R.dimen.dp_10).toInt(),
                getResources().getDimension(R.dimen.dp_20).toInt(), getResources().getDimension(R.dimen.dp_10).toInt())
            return ViewHolder(textView)
        }

        inner class ViewHolder constructor(itemView: View) : AppViewHolder(itemView) {

            private val mTextView: TextView by lazy { itemView as TextView }

            override fun onBindView(position: Int) {
                mTextView.text = getItem(position)!!.getName()
            }
        }
    }

    class AddressBean constructor(
        /** （省\市\区）的名称 */
        private val name: String?,
        /** 下一级的 Json */
        private val next: JSONObject?) {

        fun getName(): String? {
            return name
        }

        fun getNext(): JSONObject? {
            return next
        }
    }

    /**
     * 省市区数据管理类
     */
    private object AddressManager {

        /**
         * 获取省列表
         */
        fun getProvinceList(context: Context): MutableList<AddressBean?>? {
            try {
                // 省市区Json数据文件来源：https://github.com/getActivity/ProvinceJson
                val jsonArray = getProvinceJson(context) ?: return null
                val length = jsonArray.length()
                val list: ArrayList<AddressBean?> = ArrayList(length)
                for (i in 0 until length) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    list.add(AddressBean(jsonObject.getString("name"), jsonObject))
                }
                return list
            } catch (e: JSONException) {
                CrashReport.postCatchedException(e)
            }
            return null
        }

        /**
         * 获取城市列表
         *
         * @param jsonObject        城市Json
         */
        fun getCityList(jsonObject: JSONObject): MutableList<AddressBean?>? {
            return try {
                val listCity = jsonObject.getJSONArray("city")
                val length = listCity.length()
                val list: ArrayList<AddressBean?> = ArrayList(length)
                for (i in 0 until length) {
                    list.add(
                        AddressBean(
                            listCity.getJSONObject(i).getString("name"),
                            listCity.getJSONObject(i)
                        )
                    )
                }
                list
            } catch (e: JSONException) {
                CrashReport.postCatchedException(e)
                null
            }
        }

        /**
         * 获取区域列表
         *
         * @param jsonObject        区域 Json
         */
        fun getAreaList(jsonObject: JSONObject): MutableList<AddressBean?>? {
            return try {
                val listArea = jsonObject.getJSONArray("area")
                val length = listArea.length()
                val list: ArrayList<AddressBean?> = ArrayList(length)
                for (i in 0 until length) {
                    val string = listArea.getString(i)
                    list.add(AddressBean(string, null))
                }
                list
            } catch (e: JSONException) {
                CrashReport.postCatchedException(e)
                null
            }
        }

        /**
         * 获取资产目录下面文件的字符串
         */
        private fun getProvinceJson(context: Context): JSONArray? {
            try {
                val inputStream: InputStream =
                    context.resources.openRawResource(R.raw.province)
                val outStream = ByteArrayOutputStream()
                val buffer = ByteArray(512)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    outStream.write(buffer, 0, length)
                }
                outStream.close()
                inputStream.close()
                return JSONArray(outStream.toString())
            } catch (e: IOException) {
                CrashReport.postCatchedException(e)
            } catch (e: JSONException) {
                CrashReport.postCatchedException(e)
            }
            return null
        }
    }

    interface OnListener {

        /**
         * 选择完成后回调
         *
         * @param province          省
         * @param city              市
         * @param area              区
         */
        fun onSelected(dialog: BaseDialog?, province: String, city: String, area: String)

        /**
         * 点击取消时回调
         */
        fun onCancel(dialog: BaseDialog?) {}
    }
}