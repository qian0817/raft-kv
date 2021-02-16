package com.qianlei.node.store

import com.qianlei.node.NodeId
import java.io.Closeable

/**
 * 需要持久化的状态数据
 * 需要恢复 term 和 voteFor 信息
 * 如果不持久化这两个信息，可能会导致出现两次投票的问题
 *
 * @author qianlei
 */
interface NodeStore : Closeable {
    /** 当前选举的term */
    var term: Int

    /** 投票的节点 */
    var votedFor: NodeId?
}
