package com.picfix.tools.callback

import com.picfix.tools.bean.FileBean

interface DialogCallback {
    fun onSuccess(file: FileBean)
    fun onCancel()
}