package com.picfix.tools.http.request

import com.picfix.tools.bean.TencentCloudMorphResult
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MorphService {

    @POST("morphPic")
    @FormUrlEncoded
    fun morph(
        @Field("clientToken") clientToken: String,
        @Field("urls") urls: String
    ): Observable<Response<TencentCloudMorphResult>>
}
