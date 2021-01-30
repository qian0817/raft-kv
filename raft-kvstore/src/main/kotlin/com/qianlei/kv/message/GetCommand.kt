package com.qianlei.kv.message

import kotlinx.serialization.Serializable

/**
 * Get 请求
 *
 * @author qianlei
 */
@Serializable
data class GetCommand(val key: String)