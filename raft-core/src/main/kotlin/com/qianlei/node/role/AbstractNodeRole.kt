package com.qianlei.node.role

import com.qianlei.node.NodeId


/**
 * raft算法中的抽象角色，其实现有三种，分别为 follow,candidate 和 leader。
 * @see FollowerNodeRole
 * @see CandidateNodeRole
 * @see LeaderNodeRole
 */
abstract class AbstractNodeRole(var name: RoleName, term: Int) {

    var term: Int = term
        protected set

    /**
     * 取消超时或者定时任务
     */
    abstract fun cancelTimeoutOrTask()

    fun getLeaderId(selfId: NodeId): NodeId? {
        when (this) {
            is FollowerNodeRole -> return this.leaderId
            is CandidateNodeRole -> return null
            is LeaderNodeRole -> return selfId
            else -> return null
        }
    }
}
