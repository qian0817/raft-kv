package com.qianlei.kv.message

import kotlinx.serialization.Serializable

/**
 *
 * Get 响应
 *
 * @author qianlei
 */
@Serializable
data class GetCommandResponse(val value: String)