package com.picfix.tools.http.loader

import com.picfix.tools.bean.TencentCloudMorphResult
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object MorphLoader {

    fun morph(urls: String): Observable<Response<TencentCloudMorphResult>> {
        return RetrofitServiceManager.getInstance().morphService().morph(Constant.CLIENT_TOKEN, urls)
    }
}