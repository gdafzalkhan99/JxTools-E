package com.picfix.tools.http.loader

import com.picfix.tools.bean.PayStatus
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object PayStatusLoader {

    fun getPayStatus(serviceId: Int, token: String): Observable<Response<List<PayStatus>>> {
        return RetrofitServiceManager.getInstance().payStatus.getPayStatus(serviceId, token)
    }
}