package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel

/**
 *
 * @author qianlei
 */
class AppendEntriesRpcMessage(rpc: AppendEntriesRpc, sourceNodeId: NodeId, channel: Channel? = null) :
    AbstractRpcMessage<AppendEntriesRpc>(rpc, sourceNodeId, channel)