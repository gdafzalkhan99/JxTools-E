package com.picfix.tools.view.activity

import android.Manifest
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
import com.baidu.mobads.action.BaiduAction
import com.baidu.mobads.action.PrivacyStatus
import com.picfix.tools.R
import com.picfix.tools.config.Constant
import com.picfix.tools.utils.*
import com.picfix.tools.view.views.ActivityDialog
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mmkv.MMKV

class LoginActivity : FragmentActivity() {
    private lateinit var back: ImageView
    private lateinit var userAgreement: TextView
    private lateinit var privacyAgreement: TextView
    private lateinit var login: FrameLayout
    private lateinit var agree: AppCompatCheckBox
    private var mmkv = MMKV.defaultMMKV()

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
            if (Constant.OCPC) {
                requestPhonePermission {
                    openWechat()
                }
            } else {
                openWechat()
            }
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

    private fun requestPhonePermission(method: () -> Unit) {

        val mmkv = MMKV.defaultMMKV()
        val key = mmkv?.decodeLong("read_phone_permission_deny")
        if (key != null && key != 0L) {
            if (System.currentTimeMillis() - key < 3600 * 1000) {
                ToastUtil.showShort(this, "请打开必要的权限申请保证功能的正常使用")
                return
            }
        }

        LivePermissions(this).request(
            Manifest.permission.READ_PHONE_STATE
        ).observe(this, {
            when (it) {
                is PermissionResult.Grant -> {
                    //权限允许
                    if (!RomUtil.isOppo() && Constant.OCPC) {
                        BaiduAction.setPrivacyStatus(PrivacyStatus.AGREE)
                    }
                    method()
                    mmkv?.encode("read_phone_permission_deny", 0L)
                }

                is PermissionResult.Rationale -> {
                    //权限拒绝
                    ToastUtil.showShort(this, "请打开必要的权限申请保证功能的正常使用")
                    it.permissions.forEach { s ->
                        println("Rationale:${s}")//被拒绝的权限
                        mmkv?.encode("read_phone_permission_deny", System.currentTimeMillis())
                    }
                }

                is PermissionResult.Deny -> {
                    ToastUtil.showShort(this, "请打开必要的权限申请保证功能的正常使用")
                    //权限拒绝，且勾选了不再询问
                    it.permissions.forEach { s ->
                        println("deny:${s}")//被拒绝的权限
                        mmkv?.encode("read_phone_permission_deny", System.currentTimeMillis())
                    }
                }
            }
        })
    }

}