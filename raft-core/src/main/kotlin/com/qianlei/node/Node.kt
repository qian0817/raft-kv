package com.qianlei.node

import com.qianlei.node.role.RoleNameAndLeaderId
import com.qianlei.node.statemachine.StateMachine

/**
 * 暴露给上层服务的接口
 *
 * @author qianlei
 */
interface Node {
    fun start()

    fun registerStateMachine(stateMachine: StateMachine)

    fun appendLog(commandBytes: ByteArray)

    fun getRoleNameAndLeaderId(): RoleNameAndLeaderId

    fun stop()
}