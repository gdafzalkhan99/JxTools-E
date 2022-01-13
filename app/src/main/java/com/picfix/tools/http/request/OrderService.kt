package com.picfix.tools.http.request

import com.picfix.tools.bean.Order
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface OrderService {

    @POST("orderList")
    @FormUrlEncoded
    fun getOrders(
        @Field("clientToken") token: String,
        @Field("productId") productId: String
    ): Observable<Response<List<Order>>>
}