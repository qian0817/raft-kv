package com.qianlei.rpc.message

import kotlinx.serialization.Serializable

/**
 * 请求投票结果的消息
 * @author qianlei
 */
@Serializable
data class RequestVoteResult(
    /** 选举term */
    val term: Int,
    /** 是否投票 */
    val voteGranted: Boolean
)
