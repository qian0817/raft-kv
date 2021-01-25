package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel

/**
 *
 * @author qianlei
 */
class RequestVoteRpcMessage(rpc: RequestVoteRpc, sourceNodeId: NodeId, channel: Channel? = null) :
    AbstractRpcMessage<RequestVoteRpc>(rpc, sourceNodeId, channel)