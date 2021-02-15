package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel


/**
 * 抽象的 RPC 消息
 * @author qianlei
 */
abstract class AbstractRpcMessage<T>(val rpc: T, val sourceNodeId: NodeId, val channel: Channel? = null) {
    override fun toString(): String {
        return "AbstractRpcMessage(rpc=$rpc, sourceNodeId=$sourceNodeId, channel=$channel)"
    }
}