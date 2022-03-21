package com.picfix.tools.bean

data class BaiduFaceResult(
    var error_code: Int,
    var error_msg: String,
    var log_id: Long,
    var timestamp: Long,
    var result: FaceMergeResult
)
