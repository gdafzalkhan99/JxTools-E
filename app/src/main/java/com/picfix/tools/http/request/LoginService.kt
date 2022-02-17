package com.picfix.tools.http.request

import com.picfix.tools.bean.UserInfo
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface LoginService {

    @POST("thirdLogin")
    @FormUrlEncoded
    fun getUser(
        @Field("questToken") questToken: String,
        @Field("accessCode") accessCode: String,
        @Field("osType") osType: String
    ): Observable<Response<List<UserInfo>>>
}