package com.qianlei.kv.server

import com.google.common.eventbus.EventBus
import com.qianlei.kv.server.config.ConfigFactory
import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.log.MemoryLog
import com.qianlei.node.NodeContext
import com.qianlei.node.NodeGroup
import com.qianlei.node.NodeImpl
import com.qianlei.node.store.MemoryNodeStore
import com.qianlei.rpc.nio.NioConnector
import com.qianlei.schedule.DefaultScheduler
import com.qianlei.support.SingleThreadTaskExecutor
import io.netty.channel.nio.NioEventLoopGroup

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
        val node = NodeImpl(
            NodeContext(
                serverConfig.selfId,
                NodeGroup(serverConfig.groupEndpoint, serverConfig.selfId),
                MemoryLog(),
                NioConnector(NioEventLoopGroup(), true, nodeEndpoint.id, eventBus, nodeEndpoint.address.port),
                DefaultScheduler(
                    serverConfig.minElectionTimeout,
                    serverConfig.maxElectionTimeout,
                    serverConfig.logReplicationDelay,
                    serverConfig.logReplicationInterval
                ),
                eventBus,
                SingleThreadTaskExecutor(),
                MemoryNodeStore()
            )
        )
        startServer(Server(node, serverConfig.port))
    }

    private fun startServer(server: Server) {
        server.start()
        Runtime.getRuntime().addShutdownHook(Thread({ server.stop() }, "shutdown"))
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