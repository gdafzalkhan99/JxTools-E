package com.picfix.tools.http.loader

import com.picfix.tools.bean.TencentCloudResult
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object CartoonLoader {

    fun cartoonTrans(imageUrl: String): Observable<Response<TencentCloudResult>> {
        return RetrofitServiceManager.getInstance().cartoonTrans().cartoon(Constant.CLIENT_TOKEN, imageUrl)
    }
}