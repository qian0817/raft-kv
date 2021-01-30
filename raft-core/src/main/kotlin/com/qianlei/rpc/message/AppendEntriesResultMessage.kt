package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

/**
 *
 * @author qianlei
 */
@Serializable
class AppendEntriesResultMessage(
    val result: AppendEntriesResult,
    val sourceNodeId: NodeId,
    val rpc: AppendEntriesRpc
)