package com.qianlei.kv.server

import com.google.common.eventbus.EventBus
import com.qianlei.kv.message.Failure
import com.qianlei.kv.message.Redirect
import com.qianlei.kv.message.Success
import com.qianlei.kv.server.config.ConfigFactory
import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.log.MemoryLog
import com.qianlei.node.Node
import com.qianlei.node.NodeContext
import com.qianlei.node.NodeGroup
import com.qianlei.node.NodeImpl
import com.qianlei.node.store.MemoryNodeStore
import com.qianlei.rpc.nio.NioConnector
import com.qianlei.schedule.DefaultScheduler
import com.qianlei.support.SingleThreadTaskExecutor
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.netty.channel.nio.NioEventLoopGroup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        node.start()
        startHttpServer(serverConfig, node)
        Runtime.getRuntime().addShutdownHook(Thread({ node.stop() }, "shutdown"))
    }

    private fun startHttpServer(
        serverConfig: ServerConfig,
        node: Node
    ) {
        val service = Service(node, serverConfig)
        embeddedServer(Netty, serverConfig.port) {
            routing {
                get("/data/{key}") {
                    val key = call.parameters["key"]!!
                    val value = service.get(key)
                    if (value == null) {
                        call.respondText(
                            Json.encodeToString(mapOf("key" to key, "value" to null)),
                            ContentType.Application.Json,
                            HttpStatusCode.NotFound
                        )
                    } else {
                        call.respondText(
                            Json.encodeToString(mapOf("key" to key, "value" to value)),
                            ContentType.Application.Json
                        )
                    }
                }
                get("/data/{key}/{value}") {
                    val key = call.parameters["key"]!!
                    val value = call.parameters["value"]!!
                    when (service.set(key, value)) {
                        is Success -> call.respondText(
                            Json.encodeToString(mapOf("key" to key, "value" to value)),
                            ContentType.Application.Json
                        )
                        is Failure -> call.respondText(
                            Json.encodeToString(Failure),
                            ContentType.Application.Json,
                            HttpStatusCode.InternalServerError
                        )
                        is Redirect -> call.respondText(
                            Json.encodeToString(mapOf("message" to "not a leader")),
                            ContentType.Application.Json
                        )
                    }
                }
            }
        }.start(true)
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