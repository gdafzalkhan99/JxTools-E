package com.picfix.tools.http.request

import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AccountDeleteService {

    @POST("cancel")
    @FormUrlEncoded
    fun delete(
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String
    ): Observable<Response<String>>
}