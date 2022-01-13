package com.picfix.tools.http.loader

import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object AccountLoader {

    fun delete(): Observable<Response<String>> {
        return RetrofitServiceManager.getInstance().accountDelete().delete(Constant.CLIENT_TOKEN, Constant.PRODUCT_ID)
    }
}