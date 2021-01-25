package com.qianlei.rpc.message

import com.qianlei.node.NodeId

/**
 *
 * @author qianlei
 */
class AppendEntriesResultMessage(
    val result: AppendEntriesResult,
    val sourceNodeId: NodeId,
    val rpc: AppendEntriesRpc
)