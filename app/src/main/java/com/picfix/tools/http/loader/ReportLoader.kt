package com.picfix.tools.http.loader

import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object ReportLoader {

    fun report(): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().report().report(Constant.CLIENT_TOKEN)
    }
}