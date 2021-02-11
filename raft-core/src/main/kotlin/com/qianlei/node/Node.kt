package com.qianlei.node

import com.qianlei.log.statemachine.StateMachine
import com.qianlei.node.role.RoleNameAndLeaderId

/**
 * 暴露给上层服务的接口
 *
 * @author qianlei
 */
interface Node {
    fun start()

    fun appendLog(commandBytes: ByteArray)

    fun getRoleNameAndLeaderId(): RoleNameAndLeaderId

    fun stop()

    fun registerStateMachine(stateMachine: StateMachine)
}