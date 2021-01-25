package com.qianlei.rpc.message

data class RequestVoteResult(
    val term: Int,
    val voteGranted: Boolean
)
