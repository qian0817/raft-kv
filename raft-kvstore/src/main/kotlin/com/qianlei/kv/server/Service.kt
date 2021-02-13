package com.qianlei.kv.server

import com.qianlei.kv.message.Redirect
import com.qianlei.kv.message.SetCommand
import com.qianlei.kv.message.Success
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
    private val pendingCommands = ConcurrentHashMap<String, SetCommand>()
    private val map = ConcurrentHashMap<String, String>()

    init {
        node.registerStateMachine(StateMachineImpl())
    }

    fun set(key: String, value: String): Any {
        val redirect = checkLeadership()
        if (redirect != null) {
            return redirect
        }
        val command = SetCommand(key, value)
        logger.debug { "set ${command.key}" }
        pendingCommands[command.requestId] = command
        node.appendLog(command.toBytes())
        return Success
    }

    private fun checkLeadership(): Redirect? {
        val state = node.getRoleNameAndLeaderId()
        if (state.role != RoleName.LEADER) {
            return Redirect(state.leaderId)
        }
        return null
    }

    fun get(key: String?): String? {
        logger.debug { "get $key" }
        return map[key]
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
        }

        override fun shutdown() {
            taskExecutor.shutdown()
        }
    }
}