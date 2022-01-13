package com.picfix.tools.http.loader

import com.picfix.tools.bean.Price
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object ServiceListLoader {

    fun getServiceList(): Observable<Response<List<Price>>> {
        return RetrofitServiceManager.getInstance().price.getServiceList()
    }
}