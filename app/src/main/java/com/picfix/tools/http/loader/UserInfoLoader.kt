package com.picfix.tools.http.loader

import com.picfix.tools.bean.UserInfo
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object UserInfoLoader : ObjectLoader() {

    fun getUser(token: String, code: String): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.getInstance().userInfo.getUser(token, code, "AN")
    }
}