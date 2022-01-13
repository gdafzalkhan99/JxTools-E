package com.picfix.tools.http.request

import com.picfix.tools.bean.Config
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ConfigService {

    @GET("siteInfo")
    fun getConfig(@Query("serverCode") serviceCode: String): Observable<Response<Config>>
}