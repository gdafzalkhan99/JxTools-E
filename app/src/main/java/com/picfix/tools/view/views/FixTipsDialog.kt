package com.picfix.tools.view.views

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import com.picfix.tools.R
import com.picfix.tools.bean.FileBean
import com.picfix.tools.callback.Callback
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.activity.AgreementActivity


class FixTipsDialog(private val activity: Activity) : Dialog(activity, R.style.app_dialog) {

    private lateinit var ok: TextView

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_fix_tips, null)
        setContentView(dialogContent)
        setCancelable(false)

        ok = findViewById(R.id.ok)
        ok.setOnClickListener { cancel() }
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