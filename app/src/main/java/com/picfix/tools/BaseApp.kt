package com.picfix.tools

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
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

        val config = TTAdConfig.Builder().appId("5223868")
            .useTextureView(true) //默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
            .appName("照片智能修复_android")
            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)//落地页主题
            .allowShowNotify(true) //是否允许sdk展示通知栏提示,若设置为false则会导致通知栏不显示下载进度
            .debug(false) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
            .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) //允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
            .supportMultiProcess(false) //是否支持多进程，true支持
            .asyncInit(true) //是否异步初始化sdk,设置为true可以减少SDK初始化耗时。3450版本开始废弃~~
            .build()

        TTAdSdk.init(this, config, object : TTAdSdk.InitCallback {
            override fun success() {
                JLog.i("ad init success")
            }

            override fun fail(p0: Int, p1: String?) {
            }
        })
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