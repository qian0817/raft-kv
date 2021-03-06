package com.qianlei.kv.server

import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.kv.server.message.CommandRequest
import com.qianlei.kv.server.message.SetCommand
import com.qianlei.kv.server.netty.buildResponse
import com.qianlei.log.snapshot.Snapshot
import com.qianlei.log.statemachine.StateMachine
import com.qianlei.log.statemachine.StateMachineContext
import com.qianlei.node.Node
import com.qianlei.node.role.RoleNameAndLeaderId
import com.qianlei.support.SingleThreadTaskExecutor
import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import org.rocksdb.Options
import org.rocksdb.RocksDB
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author qianlei
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class RocksDBService(private val node: Node, serverConfig: ServerConfig) : KVService {
    companion object {
        init {
            RocksDB.loadLibrary()
        }
    }

    private val logger = KotlinLogging.logger { }
    private val rocksDB: RocksDB
    private val pendingCommands = ConcurrentHashMap<String, CommandRequest<*>>()

    init {
        val dbPath = serverConfig.dataPath
        val options = Options()
        options.setCreateIfMissing(true)
        // 文件不存在，则先创建文件
        if (!Files.isSymbolicLink(Paths.get(dbPath))) {
            Files.createDirectories(Paths.get(dbPath))
        }
        rocksDB = RocksDB.open(options, dbPath)
        node.registerStateMachine(StateMachineImpl())
    }

    override fun set(key: String, value: String, ctx: ChannelHandlerContext) {
        val command = SetCommand(key, value)
        logger.debug { "set ${command.key}" }
        pendingCommands[command.requestId] = CommandRequest(command, ctx.channel())
        node.appendLog(command.toBytes())
    }

    override fun getNodeState(): RoleNameAndLeaderId {
        return node.getRoleNameAndLeaderId()
    }

    override fun get(key: String): String? {
        logger.debug { "get $key" }
        return rocksDB.get(key)
    }

    override fun getAll(): Map<String, String> {
        val map = HashMap<String, String>()
        val iterator = rocksDB.newIterator()
        iterator.seekToFirst()
        while (iterator.isValid) {
            map[iterator.key().decodeToString()] = iterator.value().decodeToString()
            iterator.next()
        }
        return map
    }

    private inner class StateMachineImpl : StateMachine {
        private val logger = KotlinLogging.logger { }
        private val taskExecutor = SingleThreadTaskExecutor("state-machine")

        @Volatile
        override var lastApplied = rocksDB.get("lastApplied")?.toIntOrNull() ?: 0
            private set(value) {
                rocksDB.put("lastApplied", value.toString())
                field = value
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
            logger.info { "generateSnapshot" }
            toSnapshot(getAll(), output)
        }

        override fun applySnapshot(snapshot: Snapshot) {
            logger.info { "apply snapshot, last included index ${snapshot.lastIncludeIndex}" }
            val size = snapshot.dataSize
            val data = snapshot.dataStream.readNBytes(size.toInt())
            val map = fromSnapshot(data)
            val iterator = rocksDB.newIterator()
            iterator.seekToFirst()
            while (iterator.isValid) {
                rocksDB.delete(iterator.key())
                iterator.next()
            }
            map.forEach { (k, v) -> rocksDB.put(k, v) }
            lastApplied = snapshot.lastIncludeIndex
        }

        fun toSnapshot(map: Map<String, String>, output: OutputStream) {
            val data = ProtoBuf.encodeToByteArray(map)
            output.write(data)
        }

        fun fromSnapshot(data: ByteArray): ConcurrentHashMap<String, String> {
            val map = ConcurrentHashMap<String, String>()
            val list = ProtoBuf.decodeFromByteArray<Map<String, String>>(data)
            list.forEach { (key, value) -> map[key] = value }
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
            logger.debug { "apply log $index" }
            applyCommand(commandBytes)
            lastApplied = index
            if (shouldGenerateSnapshot(firstLogIndex, index)) {
                context.generateSnapshot(index)
            }
        }

        private fun applyCommand(commandBytes: ByteArray) {
            val command = SetCommand.fromBytes(commandBytes)
            rocksDB.put(command.key, command.value)
            val commandRequest = pendingCommands.remove(command.requestId) ?: return
            commandRequest.reply(buildResponse(mapOf("key" to command.key, "value" to command.value)))
        }

        override fun shutdown() {
            taskExecutor.shutdown()
        }
    }

    fun RocksDB.put(key: String, value: String?) {
        if (value == null) {
            delete(key.encodeToByteArray())
        } else {
            put(key.encodeToByteArray(), value.encodeToByteArray())
        }
    }

    fun RocksDB.get(key: String): String? {
        return get(key.encodeToByteArray())?.decodeToString()
    }
}