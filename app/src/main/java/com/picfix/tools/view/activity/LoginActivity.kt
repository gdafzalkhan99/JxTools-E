package com.picfix.tools.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.FragmentActivity
import com.picfix.tools.R
import com.picfix.tools.config.Constant
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.ToastUtil
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mmkv.MMKV

class LoginActivity : FragmentActivity() {
    private lateinit var back: ImageView
    private lateinit var userAgreement: TextView
    private lateinit var privacyAgreement: TextView
    private lateinit var login: FrameLayout
    private lateinit var agree: AppCompatCheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_login)
        back = findViewById(R.id.iv_back)
        userAgreement = findViewById(R.id.user_agreement)
        privacyAgreement = findViewById(R.id.privacy_agreement)
        login = findViewById(R.id.login)
        agree = findViewById(R.id.agreement_check)

        back.setOnClickListener { finish() }
        login.setOnClickListener { login() }
        userAgreement.setOnClickListener { toAgreementPage() }
        privacyAgreement.setOnClickListener { toAgreementPage() }
        initHandler()
    }

    private fun initHandler() {
        Constant.mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x1000 -> {
                        finish()
                    }
                }
            }
        }
    }


    private fun login() {
        if (agree.isChecked) {
            openWechat()
        }
    }

    private fun openWechat() {
        if (AppUtil.checkPackageInfo(this, Constant.WX_PACK_NAME)) {
            val req = SendAuth.Req()
            req.scope = "snsapi_userinfo"
            req.state = "wechat_login"
            if (Constant.api != null) {
                Constant.api.sendReq(req)
            }
        } else {
            ToastUtil.showShort(this, "请安装微信")
        }
    }


    private fun toAgreementPage() {
        val intent = Intent(this, AgreementActivity::class.java)
        startActivity(intent)
    }

}