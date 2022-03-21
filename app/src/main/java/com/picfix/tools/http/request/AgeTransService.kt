package com.picfix.tools.http.request

import com.picfix.tools.bean.TencentCloudResult
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AgeTransService {

    @POST("agePic")
    @FormUrlEncoded
    fun ageTrans(
        @Field("clientToken") clientToken: String,
        @Field("imageUrl") imageUrl: String,
        @Field("age") age: Int
    ): Observable<Response<TencentCloudResult>>
}
