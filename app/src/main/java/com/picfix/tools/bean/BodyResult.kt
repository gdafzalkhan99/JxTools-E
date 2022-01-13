package com.picfix.tools.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 15:24
 */
data class BodyResult(
    var log_id: Long,
    var labelmap: String,
    var scoremap: String,
    var foreground: String,
    var person_num: Int
)
