package com.picfix.tools.http.loader

import com.picfix.tools.bean.AlipayParam
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import com.picfix.tools.utils.AppUtil
import io.reactivex.Observable

object AliPayLoader {

    fun getOrderParam(serviceId: Int, isBack: Int): Observable<Response<AlipayParam>> {
        return RetrofitServiceManager.getInstance().aliPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId(),
            isBack
        )
    }
}