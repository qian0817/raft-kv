package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel

/**
 *
 * @author qianlei
 */
class InstallSnapshotRpcMessage(
    rpc: InstallSnapshotRpc,
    sourceNodeId: NodeId,
    channel: Channel?
) : AbstractRpcMessage<InstallSnapshotRpc>(rpc, sourceNodeId, channel)