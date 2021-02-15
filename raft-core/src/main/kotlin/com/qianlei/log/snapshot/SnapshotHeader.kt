package com.qianlei.log.snapshot

import kotlinx.serialization.Serializable

@Serializable
data class SnapshotHeader(
    val lastIndex: Int,
    val lastTerm: Int
)
