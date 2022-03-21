package com.picfix.tools.callback

interface HttpCallback {
    fun onSuccess()
    fun onFailed(msg: String)
}