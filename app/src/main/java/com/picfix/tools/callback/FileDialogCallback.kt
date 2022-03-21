package com.picfix.tools.callback

interface FileDialogCallback {
    fun onSuccess(str: String)
    fun onCancel()
}