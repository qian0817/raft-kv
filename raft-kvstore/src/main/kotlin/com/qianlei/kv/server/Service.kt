package com.qianlei.kv.server

import com.qianlei.kv.message.Redirect
import com.qianlei.kv.message.SetCommand
import com.qianlei.kv.message.Success
import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.log.statemachine.StateMachine
import com.qianlei.node.Node
import com.qianlei.node.role.RoleName
import com.qianlei.support.SingleThreadTaskExecutor
import mu.KotlinLogging
import org.rocksdb.Options
import org.rocksdb.RocksDB
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author qianlei
 */
class Service(private val node: Node, serverConfig: ServerConfig) {
    companion object {
        init {
            RocksDB.loadLibrary()
        }
    }

    private val logger = KotlinLogging.logger { }
    private val rocksDB: RocksDB
    private val pendingCommands = ConcurrentHashMap<String, SetCommand>()

    init {
        val dbPath = serverConfig.dataPath
        val options = Options()
        options.setCreateIfMissing(true)
        // 文件不存在，则先创建文件
        if (!Files.isSymbolicLink(Paths.get(dbPath))) {
            Files.createDirectories(Paths.get(dbPath))
        }
        rocksDB = RocksDB.open(options, dbPath)
    }

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
        return rocksDB.get(key.encodeToByteArray())?.decodeToString()
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
            return rocksDB.put(command.key.encodeToByteArray(), command.value.encodeToByteArray())
        }

        override fun shutdown() {
            taskExecutor.shutdown()
        }
    }
}