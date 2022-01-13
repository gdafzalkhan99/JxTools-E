package com.picfix.tools.http.request

import com.picfix.tools.bean.TencentCloudResult
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CartoonService {

    @POST("cartoonPic")
    @FormUrlEncoded
    fun cartoon(
        @Field("clientToken") clientToken: String,
        @Field("imageUrl") imageUrl: String
    ): Observable<Response<TencentCloudResult>>
}
