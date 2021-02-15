package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

/**
 *
 * @author qianlei
 */
@Serializable
data class InstallSnapshotRpc(
    /** 选举 term */
    val term: Int,
    /** Leader 节点的 ID */
    val leaderId: NodeId,
    /** 日志快照中的最后一条日志的索引 */
    val lastIndex: Int,
    /** 日志快照中的最后一条日志的 term */
    val lastTerm: Int,
    /** 数据偏移 */
    val offset: Int,
    /** 数据 */
    val data: ByteArray,
    /** 是否是最后一条消息 */
    val done: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InstallSnapshotRpc

        if (term != other.term) return false
        if (leaderId != other.leaderId) return false
        if (lastIndex != other.lastIndex) return false
        if (lastTerm != other.lastTerm) return false
        if (offset != other.offset) return false
        if (!data.contentEquals(other.data)) return false
        if (done != other.done) return false

        return true
    }

    override fun hashCode(): Int {
        var result = term
        result = 31 * result + leaderId.hashCode()
        result = 31 * result + lastIndex
        result = 31 * result + lastTerm
        result = 31 * result + offset
        result = 31 * result + data.contentHashCode()
        result = 31 * result + done.hashCode()
        return result
    }
}