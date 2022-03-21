package com.picfix.tools.http.request

import com.picfix.tools.bean.MorphResult
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GetMorphUrlService {

    @POST("getMorphUrl")
    @FormUrlEncoded
    fun getMorphUrl(
        @Field("clientToken") clientToken: String,
        @Field("jobId") jobId: String
    ): Observable<Response<MorphResult>>
}