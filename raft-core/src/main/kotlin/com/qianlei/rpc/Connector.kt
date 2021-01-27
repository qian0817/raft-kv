package com.qianlei.rpc

import com.qianlei.node.NodeEndpoint
import com.qianlei.rpc.message.AppendEntriesResult
import com.qianlei.rpc.message.AppendEntriesRpc
import com.qianlei.rpc.message.RequestVoteResult
import com.qianlei.rpc.message.RequestVoteRpc

/**
 * RPC 调用接口,方便切换实现和测试
 * @author qianlei
 */
interface Connector {
    /**
     * 初始化
     */
    fun initialize()

    /**
     * 发送[rpc]消息给多个节点[destinationEndpoints]
     */
    fun sendRequestVote(rpc: RequestVoteRpc, destinationEndpoints: Collection<NodeEndpoint>)

    /**
     * 回复[result]结果给单个节点
     */
    fun replyRequestVote(result: RequestVoteResult, destinationEndpoint: NodeEndpoint)

    /**
     * 发送[rpc]消息给单个节点
     */
    fun sendAppendEntries(rpc: AppendEntriesRpc, destinationEndpoint: NodeEndpoint)

    /**
     * 回复[result]结果给单个节点
     */
    fun replyAppendEntries(result: AppendEntriesResult, destinationEndpoint: NodeEndpoint)

    fun resetChannels()

    /**
     * 关闭连接
     */
    fun close()
}