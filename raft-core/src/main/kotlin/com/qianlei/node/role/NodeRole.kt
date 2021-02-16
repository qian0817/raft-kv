package com.qianlei.node.role

import com.qianlei.node.NodeId
import com.qianlei.schedule.ElectionTimeout
import com.qianlei.schedule.LogReplicationTask


/**
 * raft算法中的抽象角色，其实现有三种，分别为 follow,candidate 和 leader。
 * @see FollowerNodeRole
 * @see CandidateNodeRole
 * @see LeaderNodeRole
 */
sealed class NodeRole(var name: RoleName, term: Int) {

    var term: Int = term
        protected set

    /**
     * 取消超时或者定时任务
     */
    abstract fun cancelTimeoutOrTask()

    fun getLeaderId(selfId: NodeId): NodeId? {
        return when (this) {
            is FollowerNodeRole -> this.leaderId
            is CandidateNodeRole -> null
            is LeaderNodeRole -> selfId
        }
    }
}


/**
 * candidate 节点
 *
 * @author qianlei
 */
class CandidateNodeRole(
    term: Int,
    /**
     * 选举超时
     */
    private val electionTimeout: ElectionTimeout,
    /**
     * 票数
     */
    val voteCount: Int = 1
) : NodeRole(RoleName.CANDIDATE, term) {
    override fun cancelTimeoutOrTask() {
        electionTimeout.cancel()
    }

    override fun toString(): String {
        return "CandidateNodeRole(term=$term, electionTimeout=$electionTimeout, voteCount=$voteCount)"
    }
}

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
) : NodeRole(RoleName.FOLLOWER, term) {

    override fun cancelTimeoutOrTask() {
        electionTimeOut.cancel()
    }

    override fun toString(): String {
        return "FollowerNodeRole(term=$term, votedFor=$votedFor, leaderId=$leaderId, electionTimeOut=$electionTimeOut)"
    }
}

/**
 * leader 节点
 *
 * @author qianlei
 */
class LeaderNodeRole(
    term: Int,
    private val logReplicationTask: LogReplicationTask
) : NodeRole(RoleName.LEADER, term) {

    override fun cancelTimeoutOrTask() {
        logReplicationTask.cancel()
    }

    override fun toString(): String {
        return "LeaderNodeRole(term=$term, logReplicationTask=$logReplicationTask)"
    }
}
