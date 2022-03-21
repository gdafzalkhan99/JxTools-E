package com.picfix.tools.http.loader

import com.picfix.tools.bean.Config
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import com.picfix.tools.utils.AppUtil
import io.reactivex.Observable

object ConfigLoader {

    fun getConfig(): Observable<Response<Config>> {
        return RetrofitServiceManager.getInstance().config.getConfig(AppUtil.getChannelId())
    }
}