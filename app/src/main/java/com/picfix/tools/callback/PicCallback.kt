package com.picfix.tools.callback

import com.picfix.tools.bean.FileStatus
import com.picfix.tools.bean.FileWithType

interface PicCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, index: Int)
    fun onProgress(step: Enum<FileStatus>, file: FileWithType)
    fun onFailed(step: Enum<FileStatus>, message: String)
}