package com.qianlei.rpc.message

import com.qianlei.node.NodeId

data class InstallSnapshotResultMessage(
    var result: InstallSnapshotResult,
    val sourceNodeId: NodeId,
    val rpc: InstallSnapshotRpc
)
