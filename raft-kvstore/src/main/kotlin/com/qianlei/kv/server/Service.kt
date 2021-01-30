package com.qianlei.kv.server

import com.qianlei.kv.message.*
import com.qianlei.log.entry.Entry
import com.qianlei.node.Node
import com.qianlei.node.role.RoleName
import com.qianlei.node.statemachine.AbstractSingleThreadStateMachine
import com.qianlei.node.statemachine.StateMachine
import com.qianlei.node.statemachine.StateMachineContext
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author qianlei
 */
class Service(private val node: Node) : StateMachine {
    private val logger = KotlinLogging.logger { }
    private val pendingCommands = ConcurrentHashMap<String, CommandRequest<*>>()
    private val map = ConcurrentHashMap<String, ByteArray>()

    init {
        node.registerStateMachine(this)
    }

    override val lastApplied: Int
        get() = TODO("Not yet implemented")

    override fun applyLog(context: StateMachineContext, index: Int, commandBytes: ByteArray, firstLogIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun applyEntry(entry: Entry) {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
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
        commandRequest.reply(GetCommandResponse(value))
    }

    private inner class StateMachineImpl : AbstractSingleThreadStateMachine() {
        override fun applyCommand(commandBytes: ByteArray) {
            val command = SetCommand.fromBytes(commandBytes)
            map[command.key] = command.value
            pendingCommands.remove(command.requestId)?.reply(Success)
        }
    }
}