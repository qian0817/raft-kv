package com.qianlei.node.role

import com.qianlei.schedule.ElectionTimeout

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
) : AbstractNodeRole(RoleName.CANDIDATE, term) {

    override fun cancelTimeoutOrTask() {
        electionTimeout.cancel()
    }

    fun increaseVotesCount(timeout: ElectionTimeout): CandidateNodeRole {
        cancelTimeoutOrTask()
        return CandidateNodeRole(term, electionTimeout, voteCount + 1)
    }

    override fun toString(): String {
        return "CandidateNodeRole(term=$term, electionTimeout=$electionTimeout, voteCount=$voteCount)"
    }
}