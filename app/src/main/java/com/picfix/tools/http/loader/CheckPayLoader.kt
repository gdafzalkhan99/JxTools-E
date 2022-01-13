package com.picfix.tools.http.loader

import com.picfix.tools.bean.CheckPayParam
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object CheckPayLoader {

    fun checkPay(): Observable<Response<CheckPayParam>> {
        return RetrofitServiceManager.getInstance().checkPayService().checkPay(Constant.PRODUCT_ID, Constant.CLIENT_TOKEN)
    }
}