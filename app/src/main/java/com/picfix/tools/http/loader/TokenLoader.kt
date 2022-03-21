package com.picfix.tools.http.loader

import android.content.Context
import android.os.Build
import com.picfix.tools.bean.GetToken
import com.picfix.tools.bean.Token
import com.picfix.tools.config.Constant
import com.picfix.tools.controller.RetrofitServiceManager
import com.picfix.tools.http.response.Response
import com.picfix.tools.utils.AES
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.DeviceUtil
import com.tencent.mmkv.MMKV
import io.reactivex.Observable

object TokenLoader {

    fun getToken(context: Context): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.DEVICE
        val time = System.currentTimeMillis()
        val questTime = AppUtil.timeStamp2Date(time.toString(), null)
        val questFrom = AppUtil.getChannelId()
        val questToken = AES.encrypt(questFrom, AppUtil.MD5Encode((time / 1000).toString()) + questFrom)
        val productId = Constant.PRODUCT_ID

        val deviceId: String

        val mmkv = MMKV.defaultMMKV()
        var uuid = mmkv?.decodeString("uuid")
        if (uuid == null) {
            uuid = DeviceUtil.getUUID(context)
            mmkv?.encode("uuid", uuid)
            deviceId = uuid
        } else {
            deviceId = uuid
        }

        val token = GetToken(questTime, questToken, questFrom, device, deviceId, brand, mode, productId)

        return RetrofitServiceManager.getInstance().token.getToken(token)
    }
}