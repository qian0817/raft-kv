package com.qianlei.rpc.message

import com.qianlei.log.entry.Entry
import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

@Serializable
data class AppendEntriesRpc(
    val term: Int,
    val leaderId: NodeId,
    val prevLogIndex: Int = 0,
    val prevLogTerm: Int = 0,
    val entries: List<Entry> = listOf(),
    val leaderCommit: Int = 0
) {
    val lastEntryIndex: Int
        get() = if (entries.isEmpty()) prevLogIndex else entries[entries.size - 1].index
}
