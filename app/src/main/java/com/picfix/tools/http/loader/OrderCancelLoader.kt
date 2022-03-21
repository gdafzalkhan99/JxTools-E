package com.picfix.tools.http.loader

import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object OrderCancelLoader {

    fun orderCancel(orderSn: String): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().orderCancel().orderCancel(orderSn, Constant.CLIENT_TOKEN)
    }
}