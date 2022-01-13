package com.picfix.tools.http.loader

import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import io.reactivex.Observable

object ComplaintLoader : ObjectLoader() {

    fun reportComplaint(
        uid: String,
        complaintAType: String,
        phone: String,
        payAccount: String,
        problemDesc: String,
        pic: String
    ): Observable<Response<List<String?>?>> {
        return RetrofitServiceManager.getInstance().reportComplaint().reportComplaint(
            uid,
            Constant.USER_NAME,
            Constant.CLIENT_TOKEN,
            complaintAType,
            phone,
            payAccount,
            problemDesc,
            pic,
            Constant.PRODUCT_ID
        )
    }
}