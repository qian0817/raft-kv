package com.qianlei.node.role

import com.qianlei.node.NodeId
import com.qianlei.schedule.ElectionTimeout


/**
 * follower 节点
 * @author qianlei
 */
class FollowerNodeRole(
    term: Int,
    /**
     * 投过票的节点
     */
    val votedFor: NodeId? = null,
    /**
     * 当前 leader 的节点 ID
     */
    val leaderId: NodeId? = null,
    /**
     * 选举超时
     */
    private val electionTimeOut: ElectionTimeout
) : AbstractNodeRole(RoleName.FOLLOWER, term) {

    override fun cancelTimeoutOrTask() {
        electionTimeOut.cancel()
    }

    override fun toString(): String {
        return "FollowerNodeRole(term=$term, votedFor=$votedFor, leaderId=$leaderId, electionTimeOut=$electionTimeOut)"
    }
}