package com.picfix.tools.http.loader

import com.picfix.tools.bean.AlipayParam
import com.picfix.tools.bean.PayStatus
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import com.picfix.tools.utils.AppUtil
import io.reactivex.Observable

object AtfPayLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<AlipayParam>> {
        return RetrofitServiceManager.getInstance().atfPayStatus.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            AppUtil.getChannelId(),
            "1"
        )
    }
}