package com.picfix.tools.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.msdk.adapter.util.UIUtils
import com.bytedance.msdk.api.AdError
import com.bytedance.msdk.api.TTAdConstant
import com.bytedance.msdk.api.v2.GMMediationAdSdk
import com.bytedance.msdk.api.v2.GMSettingConfigCallback
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdListener
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash
import com.bytedance.sdk.openadsdk.*
import com.picfix.tools.R
import com.picfix.tools.bean.UserInfo
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.DBManager
import com.picfix.tools.http.loader.ConfigLoader
import com.picfix.tools.http.loader.ServiceListLoader
import com.picfix.tools.http.loader.TokenLoader
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.SplashUtils
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
    private lateinit var mSplashContainer: FrameLayout
    private var mSettingConfigCallback = GMSettingConfigCallback {
        openAd()
    }

    override fun setLayout(): Int {
        return R.layout.activity_splash
    }

    override fun initView() {
        textView = findViewById(R.id.splash_start)
        splashBg = findViewById(R.id.splash_bg)
        mSplashContainer = findViewById(R.id.splash_container)

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
                loadAdWithCallback()
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
        } else {
            getAccessToken()
        }
    }

    private fun initWxApi() {
        Constant.api = WXAPIFactory.createWXAPI(this, Constant.TENCENT_APP_ID, false)
    }

    private fun initTimer() {
        timer = object : CountDownTimer(5 * 1000L, 1000) {
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

    private fun getAccessToken() {
        launch(Dispatchers.IO) {
            TokenLoader.getToken(this@SplashActivity)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    Constant.QUEST_TOKEN = it.questToken
                }, {

                })
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

//                    val versionCode = AppUtil.getPackageVersionCode(this, packageName)
//                    if (versionCode.toString() == it.appVersion) {
//                        JLog.i("ad is close")
//                        Constant.AD_OPENNING = false
//                    }
                }, {

                })
        }
    }

    private fun loadAdWithCallback() {
        if (GMMediationAdSdk.configLoadSuccess()) {
            openAd()
        } else {
            GMMediationAdSdk.registerConfigCallback(mSettingConfigCallback)
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
            timer.start()
            return
        }

        val adSlot = GMAdSlotSplash.Builder()
            .setImageAdSize(UIUtils.getScreenWidth(this), UIUtils.getScreenHeight(this))
            .setSplashPreLoad(true)//开屏gdt开屏广告预加载
            .setMuted(false) //声音开启
            .setVolume(1f)//admob 声音配置，与setMuted配合使用
            .setTimeOut(5000)//设置超时
            .setSplashButtonType(TTAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)//合规设置，点击区域设置
            .setDownloadType(TTAdConstant.DOWNLOAD_TYPE_POPUP)//合规设置，下载弹窗
            .build()

        val networkRequestInfo = SplashUtils.getGMNetworkRequestInfo()

        val mTTSplashAd = GMSplashAd(this, "887719974")

        mTTSplashAd.setAdSplashListener(object : GMSplashAdListener {
            override fun onAdClicked() {
                JLog.i("ad is click")
            }

            override fun onAdShow() {
                JLog.i("ad is show")
                timer.start()
            }

            override fun onAdShowFail(p0: AdError) {
                JLog.i("ad is show fail")
                JLog.i("error = ${p0.message}")
            }

            override fun onAdSkip() {
                timer.cancel()
                jumpTo()
            }

            override fun onAdDismiss() {
            }
        })

        mTTSplashAd.loadAd(adSlot, networkRequestInfo, object : GMSplashAdLoadCallback {
            override fun onSplashAdLoadFail(p0: AdError) {
                JLog.i(p0.message)
                JLog.i(p0.code.toString())
                JLog.i(p0.thirdSdkErrorCode.toString())
                jumpTo()
            }

            override fun onSplashAdLoadSuccess() {
                JLog.i("success")
                if (Constant.AD_OPENNING) {
                    mSplashContainer.visibility = View.VISIBLE
                    mTTSplashAd.showAd(mSplashContainer)
                } else {
                    timer.start()
                }
            }

            override fun onAdLoadTimeout() {
                JLog.i("timeout")
                timer.start()
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
                loadAdWithCallback()
            }

            if (resultCode == 0x2) {
                kv?.encode("service_agree", true)
                loadAdWithCallback()
            }
        }
    }

}