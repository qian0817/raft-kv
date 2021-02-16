package com.qianlei.node

/**
 * 节点复制状态
 * Leader 节点需要记录各个节点的 nextIndex 和 matchIndex。
 * @author qianlei
 */
class ReplicatingState(nextIndex: Int = 0, matchIndex: Int = 0) {
    /** 下一条需要复制日志条目的索引 */
    var nextIndex: Int = nextIndex
        private set

    /** 已匹配日志的索引 */
    var matchIndex: Int = matchIndex
        private set

    var replicating = false
    var lastReplicatedAt = 0L

    fun backOffNextIndex(): Boolean {
        if (nextIndex > 1) {
            nextIndex--
            return true
        }
        return false
    }

    fun advance(lastEntryIndex: Int): Boolean {
        val result = matchIndex != lastEntryIndex || nextIndex != lastEntryIndex + 1
        matchIndex = lastEntryIndex
        nextIndex = lastEntryIndex + 1
        return result
    }
}