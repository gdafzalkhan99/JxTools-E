package com.picfix.tools.http.loader

import com.picfix.tools.bean.TencentCloudResult
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object GenderTransLoader {

    /**
     * @param imageUrl
     * @param gender 0:male to female  1:female to male
     */
    fun genderTrans(imageUrl: String, gender: Int): Observable<Response<TencentCloudResult>> {
        return RetrofitServiceManager.getInstance().genderTrans().genderTrans(Constant.CLIENT_TOKEN, imageUrl, gender)
    }
}