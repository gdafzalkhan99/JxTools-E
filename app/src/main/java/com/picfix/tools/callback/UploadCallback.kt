package com.picfix.tools.callback

interface UploadCallback {
    fun onSuccess(path: String)
    fun onFailed(msg: String)
}