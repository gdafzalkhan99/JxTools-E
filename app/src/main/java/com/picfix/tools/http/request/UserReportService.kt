package com.picfix.tools.http.request

import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserReportService {

    @POST("addUserLog")
    @FormUrlEncoded
    fun report(
        @Field("clientToken") token: String,
        @Field("path") path: String,
        @Field("content") content: String,
        @Field("logType") logType: String,
    ): Observable<Response<String?>>
}