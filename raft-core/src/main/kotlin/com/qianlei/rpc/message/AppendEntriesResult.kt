package com.qianlei.rpc.message

data class AppendEntriesResult(
    val term: Int,
    val success: Boolean
)
