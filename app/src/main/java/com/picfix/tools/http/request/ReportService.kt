package com.picfix.tools.http.request

import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ReportService {

    @POST("useTimesReport")
    @FormUrlEncoded
    fun report(@Field("clientToken") token: String): Observable<Response<String?>>
}