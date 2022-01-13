package com.picfix.tools.http.request

import com.picfix.tools.bean.Price
import com.picfix.tools.config.Constant
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ServiceListService {

    @GET("serverList/${Constant.PRODUCT_ID}")
    fun getServiceList(): Observable<Response<List<Price>>>
}