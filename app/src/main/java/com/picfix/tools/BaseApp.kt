package com.picfix.tools

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import com.bytedance.msdk.api.v2.*
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.hyphenate.chat.ChatClient
import com.hyphenate.helpdesk.easeui.UIProvider
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.RomUtil
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import com.tencent.smtt.sdk.QbSdk


class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initData()
        initRom()
        initHttpRequest()
        initMMKV()
        initIM()
        initBugly()
        initTTAd()
    }

    private fun initData() {
        if (AppUtil.isDebugger(this)) {
            Constant.isDebug = true
        }
    }

    /**
     * 读取设备信息
     *
     */
    private fun initRom() {
        val name = RomUtil.getName()
        if (name != null) {
            Constant.ROM = name
        } else {
            Constant.ROM = Constant.ROM_OTHER
        }
    }

    private fun initHttpRequest() {
        RetrofitServiceManager.getInstance().initRetrofitService()
    }


    /**
     * init mmkv
     */
    private fun initMMKV() {
        MMKV.initialize(this)
    }


    private fun initIM() {
        val option = ChatClient.Options()
        option.setAppkey(Constant.SDK_APP_KEY)
        option.setTenantId(Constant.SDK_TENANT_ID)

        if (!ChatClient.getInstance().init(this, option)) {
            return
        }

        UIProvider.getInstance().init(this)
    }

    private fun initBugly() {
        Bugly.init(applicationContext, Constant.BUGLY_APPID, false)
    }


    private fun initTTAd() {

        val gmPrivacyConfig = object : GMPrivacyConfig() {
            override fun isCanUsePhoneState(): Boolean {
                return true
            }
        }

        val adConfig = GMAdConfig.Builder()
            .setAppId("5223868")
            .setAppName("照片智能修复_android")
            .setDebug(false)
            .setPrivacyConfig(gmPrivacyConfig)
            .setPangleOption(
                GMPangleOption.Builder()
                    .setTitleBarTheme(GMAdConstant.TITLE_BAR_THEME_DARK)
                    .setIsUseTextureView(true)
                    .setAllowShowNotify(true)
                    .setAllowShowPageWhenScreenLock(true)
                    .setDirectDownloadNetworkType(GMAdConstant.NETWORK_STATE_WIFI, GMAdConstant.NETWORK_STATE_3G)
                    .build()
            )
            .build()

        GMMediationAdSdk.initialize(this, adConfig)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.fontScale != 1.0f) {
            resources
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

}