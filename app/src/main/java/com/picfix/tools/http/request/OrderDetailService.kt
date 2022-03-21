package com.picfix.tools.http.request

import com.picfix.tools.bean.OrderDetail
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderDetailService {

    @POST("orderDetail")
    @FormUrlEncoded
    fun getOrderDetail(@Field("orderSn") orderSn: String, @Field("clientToken") token: String): Observable<Response<OrderDetail>>
}