package com.picfix.tools.http.loader

import com.picfix.tools.bean.FastPayParam
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import com.picfix.tools.utils.AppUtil
import io.reactivex.Observable

object FastPayParamLoader {

    fun getOrderParam(serviceId: Int): Observable<Response<FastPayParam>> {
        return RetrofitServiceManager.getInstance().fastPayParam.getOrderParam(
            serviceId,
            Constant.CLIENT_TOKEN,
            Constant.PRODUCT_ID,
            AppUtil.getChannelId()
        )
    }
}