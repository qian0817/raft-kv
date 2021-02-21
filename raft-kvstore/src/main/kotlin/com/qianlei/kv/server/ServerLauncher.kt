package com.qianlei.kv.server

import com.google.common.eventbus.EventBus
import com.qianlei.kv.server.config.ConfigFactory
import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.kv.server.netty.HttpServer
import com.qianlei.log.FileLog
import com.qianlei.node.NodeContext
import com.qianlei.node.NodeGroup
import com.qianlei.node.NodeImpl
import com.qianlei.node.store.FileNodeStore
import com.qianlei.rpc.nio.NioConnector
import com.qianlei.schedule.DefaultScheduler
import com.qianlei.support.SingleThreadTaskExecutor
import io.netty.channel.nio.NioEventLoopGroup
import java.io.File

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class ServerLauncher {
    fun start(serverConfig: ServerConfig) {
        val nodeEndpoint =
            serverConfig.groupEndpoint.find { it.id == serverConfig.selfId } ?: throw IllegalArgumentException("")
        val eventBus = EventBus()
        val dataDir = File(serverConfig.dataPath)
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        val scheduler = DefaultScheduler(
            serverConfig.minElectionTimeout,
            serverConfig.maxElectionTimeout,
            serverConfig.logReplicationDelay,
            serverConfig.logReplicationInterval
        )
        val node = NodeImpl(
            NodeContext(
                serverConfig.selfId,
                NodeGroup(serverConfig.groupEndpoint, serverConfig.selfId),
                FileLog(dataDir, eventBus),
                NioConnector(NioEventLoopGroup(), true, nodeEndpoint.id, eventBus, nodeEndpoint.address.port),
                scheduler,
                eventBus,
                SingleThreadTaskExecutor(),
                FileNodeStore(File(dataDir, FileNodeStore.FILE_NAME))
            )
        )
        node.start()
        val service = RocksDBService(node, serverConfig)
        HttpServer(service, serverConfig).start()
        Runtime.getRuntime().addShutdownHook(Thread({ node.stop() }, "shutdown"))
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("please set config location")
        return
    }
    val config = ConfigFactory.parseConfig(args[0])
    val launcher = ServerLauncher()
    launcher.start(config)
}