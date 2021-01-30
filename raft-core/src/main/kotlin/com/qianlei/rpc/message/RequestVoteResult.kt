package com.qianlei.rpc.message

import kotlinx.serialization.Serializable

@Serializable
data class RequestVoteResult(
    val term: Int,
    val voteGranted: Boolean
)
