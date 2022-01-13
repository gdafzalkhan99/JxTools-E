package com.picfix.tools.http.loader

import com.picfix.tools.bean.OssParam
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object OssLoader : ObjectLoader() {

    fun getOssToken(token: String): Observable<Response<OssParam>> {
        return RetrofitServiceManager.getInstance().ossToken.getOssToken(token)
    }
}