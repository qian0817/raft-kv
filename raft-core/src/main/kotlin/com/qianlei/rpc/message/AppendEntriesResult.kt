package com.qianlei.rpc.message

import kotlinx.serialization.Serializable

/**
 * @author qianlei
 */
@Serializable
data class AppendEntriesResult(
    /** 选举term */
    val term: Int,
    /** 是否追加成功 */
    val success: Boolean
)
