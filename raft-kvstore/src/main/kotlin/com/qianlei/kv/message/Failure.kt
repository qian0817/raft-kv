package com.qianlei.kv.message

import kotlinx.serialization.Serializable

/**
 * 异常情况下的 GET 响应
 *
 * @author qianlei
 */
@Serializable
data class Failure(val errorCode: Int, val message: String)
