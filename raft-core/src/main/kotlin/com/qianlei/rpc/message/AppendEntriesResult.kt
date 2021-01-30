package com.qianlei.rpc.message

import kotlinx.serialization.Serializable

@Serializable
data class AppendEntriesResult(
    val term: Int,
    val success: Boolean
)
