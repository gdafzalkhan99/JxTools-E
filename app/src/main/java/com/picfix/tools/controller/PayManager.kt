package com.picfix.tools.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.alipay.sdk.app.PayTask
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mmkv.MMKV
import com.picfix.tools.bean.*
import com.picfix.tools.callback.PayCallback
import com.picfix.tools.config.Constant
import com.picfix.tools.http.loader.*
import com.picfix.tools.http.response.ResponseTransformer
import com.picfix.tools.http.schedulers.SchedulerProvider
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog
import com.picfix.tools.utils.ToastUtil
import com.picfix.tools.view.activity.LoginActivity
import com.picfix.tools.view.views.ActivityDialog
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class PayManager private constructor() : CoroutineScope by MainScope() {

    companion object {

        @Volatile
        private var instance: PayManager? = null

        fun getInstance(): PayManager {
            if (instance == null) {
                synchronized(PayManager::class) {
                    if (instance == null) {
                        instance = PayManager()
                    }
                }
            }

            return instance!!
        }
    }

    /**
     * 检查修复套餐
     * @param activity
     * @param result
     */
    fun checkFixPay(activity: Activity, result: (Boolean) -> Unit) {

        val mmkv = MMKV.defaultMMKV()
        when (AppUtil.getChannelId()) {
            Constant.CHANNEL_VIVO, Constant.CHANNEL_HUAWEI -> {
                val times = mmkv?.decodeString("activity_times")
                if (times == "true") {
                    result(true)
                    return
                }
            }
        }

        getPayStatus(activity, Constant.PHOTO_FIX + Constant.EXPIRE_TYPE_FOREVER) {
            val pack = it.packDetail
            if (pack.isEmpty()) {
                JLog.i("111")
                result(true)
            } else {
                JLog.i("222")
                result(false)
            }
        }
    }

    /**
     * 检查恢复套餐
     * @param activity
     * @param serviceId
     * @param result
     */
    fun checkRecoveryPay(activity: Activity, serviceId: Int, result: (Boolean) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        getPayStatus(activity, Constant.COM + Constant.EXPIRE_TYPE_FOREVER) {
            val pack = it.packDetail
            if (pack.isEmpty()) {
                result(true)
                return@getPayStatus
            }

            if (pack.size == 1) {
                if (serviceId == 1 || serviceId == 2) {
                    result(true)
                } else {
                    result(true)
                }
                return@getPayStatus
            }

            if (pack.size == 2) {
                result(false)
            }
        }
    }

    fun getPayList(result: (List<Order>) -> Unit) {

        if (Constant.CLIENT_TOKEN == "") return

        launch(Dispatchers.IO) {
            OrderLoader.getOrders()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    if (it != null) {
                        result(it)
                    }
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun getServiceList(activity: Activity, result: (List<Price>) -> Unit) {
        ServiceListLoader.getServiceList()
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({
                if (it.isNotEmpty()) {
                    result(it)
                }
            }, {
                ToastUtil.show(activity, "获取服务列表失败")
            })
    }

    @SuppressLint("CheckResult")
    fun getPayStatus(activity: Activity, serviceCode: String, success: (PayStatus) -> Unit) {
        if (Constant.CLIENT_TOKEN == "") {
            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.CLIENT_TOKEN = userInfo.client_token
            } else {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
                return
            }
        }

        val service = MMKV.defaultMMKV()?.decodeParcelable(serviceCode, Price::class.java)
        if (service != null) {
            thread {
                PayStatusLoader.getPayStatus(service.id, Constant.CLIENT_TOKEN)
                    .compose(ResponseTransformer.handleResult())
                    .compose(SchedulerProvider.getInstance().applySchedulers())
                    .subscribe({
                        success(it[0])
                    }, {
                        JLog.i("request error")
                    })
            }
        }
    }


    /**
     * 支付宝支付
     */
    @SuppressLint("CheckResult")
    fun doAliPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            AliPayLoader.getOrderParam(serviceId, 0)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkOrderStatus(activity, it, callback)
                }, {
//                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    /**
     * 人工修图支付宝支付
     */
    @SuppressLint("CheckResult")
    fun doAtfPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            AtfPayLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkOrderStatus(activity, it, callback)
                }, {
//                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    /**
     * 快付
     */
    @SuppressLint("CheckResult")
    fun doFastPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            FastPayParamLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkFastPay(activity, it, callback)
                }, {
//                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    /**
     * 微信支付
     */
    @SuppressLint("CheckResult")
    fun doWechatPay(activity: Activity, serviceId: Int, callback: PayCallback) {
        thread {
            WechatPayLoader.getOrderParam(serviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    checkWechatPay(activity, it, callback)
                }, {
//                    ToastUtil.show(activity, "发起支付请求失败")
                })
        }
    }

    private fun checkOrderStatus(activity: Activity, order: AlipayParam, callback: PayCallback) {
        launch(Dispatchers.IO) {
            JLog.i("param = ${order.body}")
            JLog.i("orderSn = ${order.orderSn}")

//            val body = "method=alipay.trade.app.pay&app_id=2021002154617471&timestamp=2021-12-14+14%3A28%3A29&format=json&version=1.0&alipay_sdk=alipay-easysdk-php-2.0.0&charset=UTF-8&sign_type=RSA2&biz_content=%7B%22subject%22%3A%22%E8%AF%AD%E9%9F%B3%E6%9C%8D%E5%8A%A1%22%2C%22out_trade_no%22%3A%22PH2021121414282961920%22%2C%22total_amount%22%3A98%7D&notify_url=http%3A%2F%2Feapi.ql-recovery.com%2FpayNotify&sign=WWLRg4rFiG%2FpYnsMn0k0cYrkTbmSZc%2FBbI8ZLHdyZIXe%2F8Qjg8PgHX79jX0I83Kw2b55NnY2lBuVLW00RE9NWUu1DNdhm3Tx67hsX%2B6uDi1uK5cExFxTHZlN8k1KLAuZ3C5q6N1pvxXTTyPoiheN5AaqBOrqMfd3sKrqFNDwOYYO95simafdd%2Fcke2FksixZiqRFZfX404yttZfAXVg9UhgJuawJgPav6oK1gN6UaSCuMCEN3UFQofzJtZ2bIq1MYyJrWzIpeDQO70ktCubX%2FrMOgSDBEgn714irrtdg8%2FgnvfywjZv9%2F%2FtJiXfhGknm9HlJEllK9GaRA0ZC5lipiQ%3D%3D"
//            val orderSn ="PH2021121414282961920"

            val task = PayTask(activity)
            val result = task.payV2(order.body, true)
            val res = PayResult(result)
            val resultStatus = res.resultStatus

            if (resultStatus == "9000") {
                JLog.i("alipay success")

                callback.progress(order.orderSn)
                callback.success()

//                OrderDetailLoader.getOrderStatus(order.orderSn)
//                    .compose(ResponseTransformer.handleResult())
//                    .compose(SchedulerProvider.getInstance().applySchedulers())
//                    .subscribe({
//                        if (it.order_sn != order.orderSn) {
//                            return@subscribe
//                        }

//                        when (it.status) {
//                            "1" -> callback.success()
//                            "0" -> callback.failed("未支付")
//                            "2" -> callback.failed("退款中")
//                            "3" -> callback.failed("已退款")
//                            "4" -> callback.failed("已取消")
//                        }
//                    }, {
//                        JLog.i("${it.message}")
//                    })

            } else {
                //支付失败，也需要发起服务端校验
                JLog.i("alipay failed")

                callback.failed("已取消")

//                launch(Dispatchers.IO) {
//                    OrderCancelLoader.orderCancel(order.orderSn)
//                        .compose(ResponseTransformer.handleResult())
//                        .compose(SchedulerProvider.getInstance().applySchedulers())
//                        .subscribe({}, {
//                            JLog.i("${it.message}")
//                        })
//                }
            }
        }

    }

    private fun checkFastPay(activity: Activity, order: FastPayParam, callback: PayCallback) {
        callback.progress(order.orderSn)

        val page = order.body
//        JLog.i("cd = ${page.orderCd}")
//        JLog.i("sign = ${page.sign}")


        //alipay
//        val intent = Intent()
//        intent.setClass(activity, FastPayActivity::class.java)
//        intent.putExtra("title","支付")
//        intent.putExtra("page", page)
//        activity.startActivity(intent)

        val url = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=$page"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)

        //wechat pay
//        val sb = StringBuffer()
//        sb.append("orderCd=")
//        sb.append(page.orderCd)
//        sb.append("&")
//        sb.append("sign=")
//        sb.append(page.sign)
//        sb.append("&")
//        val api = WXAPIFactory.createWXAPI(activity, Constant.TENCENT_APP_ID)
//        val req = WXLaunchMiniProgram.Req()
//        req.userName = Constant.TENCENT_MINI_PROGRAM_APP_ID
//        req.path = "pages/index/index?$sb"
//        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
//        api.sendReq(req)

    }

    private fun checkWechatPay(activity: Activity, order: WechatPayParam, callback: PayCallback) {
        callback.progress(order.orderSn)

        JLog.i("发起支付")
        //wechat pay
        val api = WXAPIFactory.createWXAPI(activity, Constant.TENCENT_APP_ID, false)
        api.registerApp(Constant.TENCENT_APP_ID)

        val request = PayReq()
        request.appId = Constant.TENCENT_APP_ID
        request.partnerId = Constant.TENCENT_PARTNER_ID
        request.prepayId = order.body
        request.packageValue = "Sign=WXPay"
        request.nonceStr = order.noncestr
        request.timeStamp = order.timestamp.toString()
        request.sign = order.sign

        JLog.i("appid = ${request.appId} , partnerId = ${request.partnerId}, prePayId = ${request.prepayId}, packageValue = ${request.packageValue}, nonceStr = ${request.nonceStr}, timeStamp = ${request.timeStamp}, sign = ${request.sign}")

        api.sendReq(request)
    }

}