package com.qianlei.node

/**
 * 集群成员
 */
data class GroupMember(
    /**
     * 节点服务器的 IP 信息和端口信息
     */
    val endpoint: NodeEndpoint,
    /**
     * 复制进度
     */
    var replicatingState: ReplicatingState? = null,
) {
    fun idEquals(id: NodeId): Boolean {
        return endpoint.id == id
    }

    fun isReplicationStateSet(): Boolean {
        return replicatingState != null
    }

    /** 获取复制进度 */
    private fun ensureReplicatingState(): ReplicatingState {
        return replicatingState ?: throw IllegalStateException("replication state not set")
    }

    fun getNextIndex(): Int {
        return ensureReplicatingState().nextIndex
    }

    fun getMatchIndex(): Int {
        return ensureReplicatingState().matchIndex
    }

    fun advanceReplicatingState(lastEntryIndex: Int): Boolean {
        return ensureReplicatingState().advance(lastEntryIndex)
    }

    fun backOffNextIndex(): Boolean {
        return ensureReplicatingState().backOffNextIndex()
    }

    fun replicateNow() {
        replicateAt(System.currentTimeMillis())
    }

    fun replicateAt(replicatedAt: Long) {
        val replicatingState = ensureReplicatingState()
        replicatingState.replicating = true
        replicatingState.lastReplicatedAt = replicatedAt
    }

    fun isReplicating(): Boolean {
        return ensureReplicatingState().replicating
    }

    fun stopReplicating() {
        ensureReplicatingState().replicating = false
    }

    fun shouldReplicate(readTimeout: Long): Boolean {
        val replicatingState = ensureReplicatingState()
        return !replicatingState.replicating ||
                System.currentTimeMillis() - replicatingState.lastReplicatedAt >= readTimeout
    }

}