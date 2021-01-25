package com.qianlei.rpc.message

import com.qianlei.log.entry.Entry
import com.qianlei.node.NodeId

data class AppendEntriesRpc(
    val term: Int,
    val leaderId: NodeId,
    val prevLogIndex: Int = 0,
    val prevLogTerm: Int = 0,
    val entries: List<Entry> = listOf(),
    val leaderCommit: Int = 0
)
