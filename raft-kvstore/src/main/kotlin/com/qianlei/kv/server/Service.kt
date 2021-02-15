package com.qianlei.kv.server

import com.qianlei.kv.message.Redirect
import com.qianlei.kv.message.SetCommand
import com.qianlei.kv.message.Success
import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.log.entry.EntryList
import com.qianlei.log.snapshot.Snapshot
import com.qianlei.log.statemachine.StateMachine
import com.qianlei.log.statemachine.StateMachineContext
import com.qianlei.node.Node
import com.qianlei.node.role.RoleName
import com.qianlei.support.SingleThreadTaskExecutor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class Service(private val node: Node, serverConfig: ServerConfig) {

    private val logger = KotlinLogging.logger { }
    private var map = ConcurrentHashMap<String, String>()
    private val pendingCommands = ConcurrentHashMap<String, SetCommand>()

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

    fun get(key: String): String? {
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

        override fun applyLog(context: StateMachineContext, index: Int, commandBytes: ByteArray, firstLogIndex: Int) {
            taskExecutor.submit {
                doApplyLog(context, index, commandBytes, firstLogIndex)
            }
        }

        override fun shouldGenerateSnapshot(firstLogIndex: Int, lastApplied: Int): Boolean {
            return lastApplied - firstLogIndex > 1
        }

        override fun generateSnapshot(output: OutputStream) {
            toSnapshot(map, output)
        }

        override fun applySnapshot(snapshot: Snapshot) {
            logger.info { "apply snapshot, last included index ${snapshot.lastIncludeIndex}" }
            val size = snapshot.dataSize
            val data = snapshot.dataStream.readNBytes(size.toInt())
            map = fromSnapshot(data)
            lastApplied = snapshot.lastIncludeIndex
        }

        fun toSnapshot(map: Map<String, String>, output: OutputStream) {
            val list = map.map { it.key to it.value }.toList()
            val data = ProtoBuf.encodeToByteArray(EntryList(list))
            output.write(data)
        }

        fun fromSnapshot(data: ByteArray): ConcurrentHashMap<String, String> {
            val map = ConcurrentHashMap<String, String>()
            val list = ProtoBuf.decodeFromByteArray<EntryList>(data)
            list.entryList.forEach { map[it.first] = it.second }
            return map
        }

        private fun doApplyLog(
            context: StateMachineContext,
            index: Int,
            commandBytes: ByteArray,
            firstLogIndex: Int
        ) {
            if (index <= lastApplied) {
                return
            }
            logger.debug("apply log {}", index)
            applyCommand(commandBytes)
            lastApplied = index
            if (shouldGenerateSnapshot(firstLogIndex, index)) {
                context.generateSnapshot(index)
            }
        }

        private fun applyCommand(commandBytes: ByteArray) {
            val command = SetCommand.fromBytes(commandBytes)
            map[command.key] = command.value
        }

        override fun shutdown() {
            taskExecutor.shutdown()
        }
    }
}