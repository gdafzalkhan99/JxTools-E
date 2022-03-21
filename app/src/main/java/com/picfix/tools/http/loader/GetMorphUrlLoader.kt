package com.picfix.tools.http.loader

import com.picfix.tools.bean.MorphResult
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object GetMorphUrlLoader {

    fun getMorphUrl(jobId: String): Observable<Response<MorphResult>> {
        return RetrofitServiceManager.getInstance().morphUrlService.getMorphUrl(Constant.CLIENT_TOKEN, jobId)
    }
}