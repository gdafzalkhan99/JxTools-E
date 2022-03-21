package com.picfix.tools.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import com.picfix.tools.R
import com.picfix.tools.callback.Callback
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.ToastUtil


class AccountDeleteDialog(context: Context, callback: Callback) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private val mCallback: Callback = callback
    private lateinit var ok: Button
    private lateinit var agree: RadioButton

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_not_find_file, null)
        setContentView(dialogContent)
        setCancelable(true)

        ok = dialogContent.findViewById(R.id.ok)
        agree = dialogContent.findViewById(R.id.radio_agree)

        ok.setOnClickListener {
            if (agree.isChecked) {
                cancel()
                mCallback.onSuccess()
            } else {
                ToastUtil.showShort(mContext, "请勾选同意注销账号")
            }
        }

    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) - 50
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}