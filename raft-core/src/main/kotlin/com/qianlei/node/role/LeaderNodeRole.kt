package com.qianlei.node.role

import com.qianlei.schedule.LogReplicationTask


/**
 * leader 节点
 *
 * @author qianlei
 */
class LeaderNodeRole(
    term: Int,
    private val logReplicationTask: LogReplicationTask
) : AbstractNodeRole(RoleName.LEADER, term) {

    override fun cancelTimeoutOrTask() {
        logReplicationTask.cancel()
    }

    override fun toString(): String {
        return "LeaderNodeRole(term=$term, logReplicationTask=$logReplicationTask)"
    }
}
