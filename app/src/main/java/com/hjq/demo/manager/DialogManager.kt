package com.hjq.demo.manager

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hjq.base.BaseDialog

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2021/01/29
 *    desc   : Dialog 显示管理类
 */
class DialogManager private constructor(lifecycleOwner: LifecycleOwner) :
    LifecycleEventObserver, BaseDialog.OnDismissListener {

    companion object {

        private val DIALOG_MANAGER = HashMap<LifecycleOwner, DialogManager>()

        fun getInstance(lifecycleOwner: LifecycleOwner): DialogManager {
            var manager = DIALOG_MANAGER[lifecycleOwner]
            if (manager == null) {
                manager = DialogManager(lifecycleOwner)
                DIALOG_MANAGER[lifecycleOwner] = manager
            }
            return manager
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    var dialogList: MutableList<BaseDialog?> = ArrayList()
    private set

    private val dialogPriority = HashMap<BaseDialog?, Int>()

    /**
     * 添加 Dialog 对象
     *
     * @param priority        弹窗优先级
     */
    fun addDialog(dialog: BaseDialog?, priority: Int = 0) {
        if (dialog == null) {
            return
        }
        if (dialogList.contains(dialog)) {
            return
        }
        var dialogIndex = dialogList.size
        for (i in dialogList.indices) {
            val itemDialog = dialogList[i]
            val itemPriority = dialogPriority[itemDialog] ?: continue
            if (priority > itemPriority && !itemDialog!!.isShowing) {
                dialogIndex = i
            }
        }
        dialogList.add(dialogIndex, dialog)
        dialogPriority[dialog] = priority
    }

    /**
     * 排队显示 Dialog
     */
    fun startShow() {
        if (dialogList.isEmpty()) {
            return
        }
        val firstDialog = dialogList[0]
        firstDialog?.let {
            if (it.isShowing) {
                return@let
            }
            it.addOnDismissListener(this)
            it.show()
        }
    }

    /**
     * 取消所有 Dialog 的显示
     */
    fun clearShow() {
        if (dialogList.isEmpty()) {
            return
        }
        val firstDialog = dialogList[0]
        firstDialog?.let {
            if (!it.isShowing) {
                return@let
            }
            it.removeOnDismissListener(this)
            it.dismiss()
        }
        dialogList.clear()
        dialogPriority.clear()
    }

    override fun onDismiss(dialog: BaseDialog?) {
        dialog?.removeOnDismissListener(this)
        dialogList.remove(dialog)
        dialogPriority.remove(dialog)
        for (nextDialog in dialogList) {
            if (nextDialog == null) {
                continue
            }
            if (!nextDialog.isShowing) {
                nextDialog.addOnDismissListener(this)
                nextDialog.show()
                break
            }
        }
    }

    /**
     * [LifecycleEventObserver]
     */
    override fun onStateChanged(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event) {
        if (event != Lifecycle.Event.ON_DESTROY) {
            return
        }
        DIALOG_MANAGER.remove(lifecycleOwner)
        lifecycleOwner.lifecycle.removeObserver(this)
        clearShow()
    }
}