package com.qianlei.node.store

import com.qianlei.node.NodeId
import java.io.Closeable

/**
 * 将角色状态持久化
 *
 * @author qianlei
 */
interface NodeStore : Closeable {
    var term: Int
    var votedFor: NodeId?
}
