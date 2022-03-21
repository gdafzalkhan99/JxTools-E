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


class AuthDialog(private val activity: Activity, callback: Callback) : Dialog(activity, R.style.app_dialog) {

    private lateinit var refuse: TextView
    private lateinit var agree: TextView
    private var mCallback = callback
    private lateinit var check: AppCompatCheckBox
    private lateinit var userAgreement: TextView
    private lateinit var privacyAgreement: TextView


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_service_auth, null)
        setContentView(dialogContent)
        setCancelable(false)

        refuse = dialogContent.findViewById(R.id.service_refuse)
        agree = dialogContent.findViewById(R.id.service_agree)
        check = dialogContent.findViewById(R.id.agreement_check)
        userAgreement = dialogContent.findViewById(R.id.user_agreement)
        privacyAgreement = dialogContent.findViewById(R.id.privacy_agreement)

        userAgreement.setOnClickListener { toAgreementPage() }
        privacyAgreement.setOnClickListener { toAgreementPage() }

        refuse.setOnClickListener {
            mCallback.onCancel()
            cancel()
        }

        agree.setOnClickListener {
            if (check.isChecked) {
                mCallback.onSuccess()
                cancel()
            } else {
                ToastUtil.showShort(activity, "请仔细阅读隐私协议和用户协议，同意请勾选")
            }

        }

    }

    private fun toAgreementPage() {
        val intent = Intent(activity, AgreementActivity::class.java)
        activity.startActivity(intent)
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