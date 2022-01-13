package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.sdk.openadsdk.*
import com.picfix.tools.R
import com.picfix.tools.bean.UserInfo
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.DBManager
import com.picfix.tools.http.loader.ConfigLoader
import com.picfix.tools.http.loader.ServiceListLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.base.BaseActivity
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import kotlinx.android.synthetic.main.d_pics.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
@author ZW
@description:
@date : 2020/11/25 10:31
 */
class SplashActivity : BaseActivity() {
    private lateinit var textView: TextView
    private lateinit var splashBg: ImageView
    private lateinit var timer: CountDownTimer
    private var kv = MMKV.defaultMMKV()
    private var show = false

    override fun setLayout(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        textView = findViewById(R.id.splash_start)
        splashBg = findViewById(R.id.splash_bg)

        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }

        initUserInfo()
        initWxApi()
        initTimer()
        clearDatabase()
        getServiceList()

    }


    override fun initData() {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && !show) {
            val value = kv?.decodeBool("service_agree")
            if (value == null || !value) {
                val intent = Intent(this, AgreementActivity::class.java)
                startActivityForResult(intent, 0x1)
                show = true
            } else {
                getConfig()
            }
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private fun initUserInfo() {
        val userInfo = kv?.decodeParcelable("userInfo", UserInfo::class.java)
        if (userInfo != null) {
            Constant.CLIENT_TOKEN = userInfo.client_token
            Constant.USER_NAME = userInfo.nickname
            Constant.USER_ID = userInfo.id.toString()
        }
    }


    private fun initWxApi() {
        Constant.api = WXAPIFactory.createWXAPI(this, Constant.TENCENT_APP_ID, false)
    }

    private fun initTimer() {
        timer = object : CountDownTimer(3 * 1000L, 1000) {
            override fun onFinish() {
                jumpTo()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
    }

    private fun jumpTo() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearDatabase() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(this@SplashActivity)
        }
    }


    @SuppressLint("CheckResult")
    private fun getConfig() {
        thread {
            ConfigLoader.getConfig()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.WEBSITE = it.offcialSite
                    Constant.APP_VERSION = it.appVersion

                    val versionCode = AppUtil.getPackageVersionCode(this, packageName)
                    if (versionCode.toString() == it.appVersion) {
                        JLog.i("ad is close")
                        Constant.AD_OPENNING = false
                    }

                    openAd()
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    private fun getServiceList() {
        thread {
            ServiceListLoader.getServiceList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it.isNotEmpty()) {
                        for (child in it) {
                            //save service list
                            MMKV.defaultMMKV()?.encode(child.server_code + child.expire_type, child)
                        }
                    }
                }, {
                    ToastUtil.show(this@SplashActivity, "获取服务列表失败")
                })
        }
    }

    private fun openAd() {

        if (!Constant.AD_OPENNING) {
            JLog.i("ad is not open")
            timer.start()
            return
        }

        val mTTAdNative = TTAdSdk.getAdManager().createAdNative(this)
        val adSlot = AdSlot.Builder()
            .setCodeId("887589004")
            .setImageAcceptedSize(1080, 1920)
            .setAdLoadType(TTAdLoadType.LOAD)
            .build()

        mTTAdNative.loadSplashAd(adSlot, object : TTAdNative.SplashAdListener {
            override fun onError(code: Int, message: String?) {
                JLog.i("error code = $code")
                jumpTo()
            }

            override fun onTimeout() {
                JLog.i("on timeout")
                jumpTo()
            }

            override fun onSplashAdLoad(ad: TTSplashAd?) {

                if (ad == null) {
                    JLog.i("ad is null")
                    return
                }

                JLog.i("ad is not null")

                val view = ad.splashView
                if (view != null) {
                    (splashBg.parent as ViewGroup).addView(view)
                }

                ad.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener {
                    override fun onAdClicked(p0: View?, p1: Int) {
                        timer.cancel()
                    }

                    override fun onAdShow(p0: View?, p1: Int) {
                        if (p0 != null) {
                            JLog.i("ad is show")
                            timer.start()
                        }
                    }

                    override fun onAdSkip() {
                        timer.cancel()
                        jumpTo()
                    }

                    override fun onAdTimeOver() {
//                        jumpTo()
                    }
                })
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1) {
            if (resultCode == 0x1) {
                kv?.encode("service_agree", true)
                getConfig()
            }

            if (resultCode == 0x2) {
                kv?.encode("service_agree", true)
                getConfig()
            }
        }
    }

}