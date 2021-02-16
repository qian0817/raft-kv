package com.qianlei.rpc.message

import com.qianlei.log.entry.Entry
import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

/**
 *
 * @author qianlei
 */
@Serializable
data class AppendEntriesRpc(
    /** 选举term */
    val term: Int,
    /** leader 节点 ID */
    val leaderId: NodeId,
    /** 前一条日志的索引 */
    val prevLogIndex: Int = 0,
    /** 前一条日志的 term */
    val prevLogTerm: Int = 0,
    /** 复制的日志索引 */
    val entries: List<Entry> = listOf(),
    /** leader 的 commitIndex */
    val leaderCommit: Int = 0
) {
    val lastEntryIndex: Int
        get() = if (entries.isEmpty()) prevLogIndex else entries[entries.size - 1].index
}
