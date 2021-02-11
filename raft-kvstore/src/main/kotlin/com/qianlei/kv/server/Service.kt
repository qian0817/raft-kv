package com.qianlei.kv.server

import com.qianlei.kv.message.*
import com.qianlei.log.statemachine.StateMachine
import com.qianlei.node.Node
import com.qianlei.node.role.RoleName
import com.qianlei.support.SingleThreadTaskExecutor
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author qianlei
 */
class Service(private val node: Node) {
    private val logger = KotlinLogging.logger { }
    private val pendingCommands = ConcurrentHashMap<String, CommandRequest<*>>()
    private val map = ConcurrentHashMap<String, String>()

    init {
        node.registerStateMachine(StateMachineImpl())
    }

    fun set(commandRequest: CommandRequest<SetCommand>) {
        val redirect = checkLeadership()
        if (redirect != null) {
            commandRequest.reply(redirect)
            return
        }
        val command = commandRequest.command
        logger.debug { "set ${command.key}" }
        pendingCommands[command.requestId] = commandRequest
        commandRequest.addCloseListener { pendingCommands.remove(command.requestId) }
        node.appendLog(command.toBytes())
    }

    private fun checkLeadership(): Redirect? {
        val state = node.getRoleNameAndLeaderId()
        if (state.role != RoleName.LEADER) {
            return Redirect(state.leaderId)
        }
        return null
    }

    fun get(commandRequest: CommandRequest<GetCommand>) {
        val key = commandRequest.command.key
        logger.debug { "get $key" }
        val value = map[key]
        commandRequest.reply(GetCommandResponse(value ?: "null"))
    }


    private inner class StateMachineImpl : StateMachine {
        private val logger = KotlinLogging.logger { }
        private val taskExecutor = SingleThreadTaskExecutor("state-machine")

        @Volatile
        private var lastApplied = 0
        override fun getLastApplied(): Int {
            return lastApplied
        }

        override fun applyLog(index: Int, commandBytes: ByteArray) {
            taskExecutor.submit { doApplyLog(index, commandBytes) }
        }

        private fun doApplyLog(
            index: Int,
            commandBytes: ByteArray
        ) {
            if (index <= lastApplied) {
                return
            }
            logger.debug("apply log {}", index)
            applyCommand(commandBytes)
            lastApplied = index
        }

        private fun applyCommand(commandBytes: ByteArray) {
            val command = SetCommand.fromBytes(commandBytes)
            println("set ${command.key} ${command.value}")
            map[command.key] = command.value
            pendingCommands.remove(command.requestId)?.reply(Success)
        }

        override fun shutdown() {
            taskExecutor.shutdown()
        }
    }
}