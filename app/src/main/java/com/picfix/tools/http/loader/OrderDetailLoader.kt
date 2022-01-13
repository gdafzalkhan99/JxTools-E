package com.picfix.tools.http.loader

import com.picfix.tools.bean.OrderDetail
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object OrderDetailLoader {

    fun getOrderStatus(orderSn: String): Observable<Response<OrderDetail>> {
        return RetrofitServiceManager.getInstance().orderDetail.getOrderDetail(orderSn, Constant.CLIENT_TOKEN)
    }
}