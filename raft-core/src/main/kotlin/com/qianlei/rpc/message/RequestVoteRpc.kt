package com.qianlei.rpc.message

import com.qianlei.node.NodeId
import kotlinx.serialization.Serializable

/**
 * 请求投票的消息
 * @author qianlei
 */
@Serializable
data class RequestVoteRpc(
    /** 选举term */
    val term: Int,
    /** 候选者节点 ID，也就是发送者自己 */
    val candidateId: NodeId,
    /** 候选者最后一条日志的索引 */
    val lastLogIndex: Int = 0,
    /** 候选者的最后一条日志的term */
    val lastLogTerm: Int = 0
)
