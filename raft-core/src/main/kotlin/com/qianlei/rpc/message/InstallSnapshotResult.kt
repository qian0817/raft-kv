package com.qianlei.rpc.message

import kotlinx.serialization.Serializable

@Serializable
data class InstallSnapshotResult(
    val term: Int
)
