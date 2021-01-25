package com.qianlei.rpc.message

import com.qianlei.node.NodeId

data class RequestVoteRpc(
    val term: Int,
    val candidateId: NodeId,
    val lastLogIndex: Int = 0,
    val lastLogTerm: Int = 0
)
