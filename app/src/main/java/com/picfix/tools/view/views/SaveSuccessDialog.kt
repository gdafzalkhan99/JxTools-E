package com.picfix.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.picfix.tools.R
import com.picfix.tools.callback.Callback
import com.picfix.tools.utils.AppUtil


class SaveSuccessDialog(context: Context, titleName: String, callback: Callback) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private val mCallback = callback
    private val mTitleName = titleName
    private lateinit var title: TextView
    private lateinit var export: TextView
    private lateinit var cancel: Button

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_save_image, null)
        setContentView(dialogContent)
        setCancelable(false)

        title = dialogContent.findViewById(R.id.export_title)
        export = dialogContent.findViewById(R.id.export_path)

        cancel = dialogContent.findViewById(R.id.dialog_cancel)
        cancel.setOnClickListener {
            cancel()
            mCallback.onSuccess()
        }

        val text = "已保存至${mTitleName}"
        title.text = text

    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 3 / 4
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}