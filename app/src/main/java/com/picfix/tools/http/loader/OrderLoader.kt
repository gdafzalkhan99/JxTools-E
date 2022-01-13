package com.picfix.tools.http.loader

import com.picfix.tools.bean.Order
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object OrderLoader {

    fun getOrders(): Observable<Response<List<Order>>> {
        return RetrofitServiceManager.getInstance().orders.getOrders(Constant.CLIENT_TOKEN, Constant.PRODUCT_ID)
    }
}