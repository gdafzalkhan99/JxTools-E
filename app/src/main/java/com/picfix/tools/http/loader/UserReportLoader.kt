package com.picfix.tools.http.loader

import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object UserReportLoader {

    fun report(path: String, content: String, logType: String): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().userReport().report(Constant.CLIENT_TOKEN, path, content, logType)
    }
}