package com.hjq.demo.ui.activity.account

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.hjq.base.BaseDialog
import com.hjq.base.ktx.lazyFindViewById
import com.hjq.demo.R
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.app.AppActivity
import com.hjq.demo.http.glide.GlideApp
import com.hjq.demo.ui.activity.common.ImagePreviewActivity
import com.hjq.demo.ui.activity.common.ImageSelectActivity
import com.hjq.demo.ui.dialog.common.AddressDialog
import com.hjq.demo.ui.dialog.common.InputDialog
import com.hjq.widget.layout.SettingBar

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/04/20
 *    desc   : 个人资料
 */
class PersonalDataActivity : AppActivity() {

    private val avatarLayout: ViewGroup? by lazyFindViewById(R.id.fl_person_data_avatar)
    private val avatarView: ImageView? by lazyFindViewById(R.id.iv_person_data_avatar)
    private val idView: SettingBar? by lazyFindViewById(R.id.sb_person_data_id)
    private val nameView: SettingBar? by lazyFindViewById(R.id.sb_person_data_name)
    private val addressView: SettingBar? by lazyFindViewById(R.id.sb_person_data_address)

    /** 省 */
    private var province: String? = "广东省"

    /** 市 */
    private var city: String? = "广州市"

    /** 区 */
    private var area: String? = "天河区"

    /** 头像地址 */
    private var avatarUrl: Uri? = null

    override fun getLayoutId(): Int {
        return R.layout.personal_data_activity
    }

    override fun initView() {
        setOnClickListener(avatarLayout, avatarView, nameView, addressView)
    }

    override fun initData() {
        avatarView?.let {
            GlideApp.with(this)
                .load(R.drawable.avatar_placeholder_ic)
                .placeholder(R.drawable.avatar_placeholder_ic)
                .error(R.drawable.avatar_placeholder_ic)
                .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                .into(it)
        }

        idView?.setRightText("880634")
        nameView?.setRightText("Android 轮子哥")
        val address: String = province + city + area
        addressView?.setRightText(address)
    }

    @SingleClick
    override fun onClick(view: View) {
        if (view === avatarLayout) {
            ImageSelectActivity.start(this, object : ImageSelectActivity.OnImageSelectListener {

                override fun onSelected(data: MutableList<String>) {
                    // 裁剪头像
                    cropImageFile(data[0])
                }
            })
        } else if (view === avatarView) {
            if (avatarUrl != null) {
                // 查看头像
                ImagePreviewActivity.start(this, avatarUrl.toString())
            } else {
                avatarLayout?.let {
                    // 选择头像
                    onClick(it)
                }
            }
        } else if (view === nameView) {
            InputDialog.Builder(this)
                .setTitle(getString(R.string.personal_data_name_hint))
                .setContent(nameView?.getRightText()) //.setHint(getString(R.string.personal_data_name_hint))
                //.setConfirm("确定")
                // 设置 null 表示不显示取消按钮
                //.setCancel("取消")
                // 设置点击按钮后不关闭对话框
                //.setAutoDismiss(false)
                .setListener(object : InputDialog.OnListener {

                    override fun onConfirm(dialog: BaseDialog?, content: String) {
                        nameView?.let {
                            if (it.getRightText() != content) {
                                it.setRightText(content)
                            }
                        }
                    }
                })
                .show()
        } else if (view === addressView) {
            AddressDialog.Builder(this) //.setTitle("选择地区")
                // 设置默认省份
                .setProvince(province) // 设置默认城市（必须要先设置默认省份）
                .setCity(city) // 不选择县级区域
                //.setIgnoreArea()
                .setListener(object : AddressDialog.OnListener {

                    override fun onSelected(dialog: BaseDialog?, province: String, city: String, area: String) {
                        addressView?.let {
                            val address: String = province + city + area
                            if (it.getRightText() != address) {
                                this@PersonalDataActivity.province = province
                                this@PersonalDataActivity.city = city
                                this@PersonalDataActivity.area = area
                                it.setRightText(address)
                            }
                        }
                    }
                })
                .show()
        }
    }

    /**
     * 裁剪图片
     */
    private fun cropImageFile(imagePath: String) {
        avatarUrl = imagePath.toUri()
        avatarView?.let {
            GlideApp.with(this)
                .load(imagePath)
                .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                .into(it)
        }
    }
}