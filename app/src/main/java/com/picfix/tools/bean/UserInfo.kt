package com.picfix.tools.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    var id: Int,
    var nickname: String,
    var avatar: String,
    var user_type: Int,
    var addtime: Long,
    var last_logintime: Long,
    var login_ip: String,
    var popularize_id: Int,
    var pop_name: String,
    var third_token: String?,
    var client_token: String,
    var device: String,
    var device_id: String,
    var brand: String,
    var mode: String,
    var city: String,
    var product_id: Int,
    var product_name: String,
    var photo_times: Int?
) : Parcelable
