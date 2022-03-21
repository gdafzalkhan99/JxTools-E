package com.picfix.tools.http.request

import com.picfix.tools.bean.GetToken
import com.picfix.tools.bean.Token
import com.picfix.tools.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("getQuestToken")
    fun getToken(@Body getToken: GetToken): Observable<Response<Token>>
}