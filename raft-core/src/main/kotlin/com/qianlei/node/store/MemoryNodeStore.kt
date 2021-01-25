package com.qianlei.node.store

import com.qianlei.node.NodeId

/**
 * 基于内存的持久化实现，主要用于测试
 * @author qianlei
 */
data class MemoryNodeStore(
    override var term: Int = 0,
    override var votedFor: NodeId? = null
) : NodeStore {
    override fun close() {}
}
