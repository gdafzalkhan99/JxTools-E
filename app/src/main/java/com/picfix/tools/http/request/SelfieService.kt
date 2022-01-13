package com.picfix.tools.http.request

import com.picfix.tools.bean.CheckPayParam
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SelfieService {

    @POST("cspayStatus")
    @FormUrlEncoded
    fun checkPay(
        @Field("clientToken") clientToken: String,
    ): Observable<Response<CheckPayParam>>
}