package com.qianlei.kv.server

import com.google.common.eventbus.EventBus
import com.qianlei.log.MemoryLog
import com.qianlei.node.*
import com.qianlei.node.store.MemoryNodeStore
import com.qianlei.rpc.nio.NioConnector
import com.qianlei.schedule.DefaultScheduler
import com.qianlei.support.SingleThreadTaskExecutor
import io.netty.channel.nio.NioEventLoopGroup
import mu.KotlinLogging
import org.apache.commons.cli.*

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class ServerLauncher {
    private val logger = KotlinLogging.logger {}

    fun start(args: Array<String>) {
        val options = buildOptions()
        if (args.isEmpty()) {
            val formatter = HelpFormatter()
            formatter.printHelp("raft-kv [OPTION]...", options)
            return
        }
        val parser = DefaultParser()
        try {
            val cmdLine = parser.parse(options, args)
            startAsGroupMember(cmdLine)
        } catch (e: java.lang.Exception) {
            when (e) {
                is ParseException, is IllegalArgumentException -> logger.error(e) {}
                else -> throw e
            }
        }
    }

    private fun startAsGroupMember(cmdLine: CommandLine) {
        require(cmdLine.hasOption("gc")) { "group config required" }
        val rawGroupConfig = cmdLine.getOptionValues("gc")
        val rawNodeId = cmdLine.getOptionValue("i")
        val portService = (cmdLine.getParsedOptionValue("p2") as Long).toInt()
        val nodeEndpoints = rawGroupConfig.map { parseNodeEndpoint(it) }.toSet()
        val nodeId = NodeId.of(rawNodeId)
        val nodeEndpoint = nodeEndpoints.find { it.id.value == rawNodeId } ?: throw IllegalArgumentException("")
        val eventBus = EventBus()
        val node = NodeImpl(
            NodeContext(
                nodeId,
                NodeGroup(nodeEndpoints, nodeId),
                MemoryLog(),
                NioConnector(NioEventLoopGroup(), true, nodeEndpoint.id, eventBus, nodeEndpoint.address.port),
                DefaultScheduler(20000, 40000, 5000, 5000),
                eventBus,
                SingleThreadTaskExecutor(),
                MemoryNodeStore()
            )
        )
        startServer(Server(node, portService))
    }

    private fun startServer(server: Server) {
        server.start()
        Runtime.getRuntime().addShutdownHook(Thread({ server.stop() }, "shutdown"))
    }


    private fun parseNodeEndpoint(rawNodeEndpoint: String): NodeEndpoint {
        val pieces = rawNodeEndpoint.split(",")
        require(pieces.size == 3) { "illegal node endpoint [ $rawNodeEndpoint ]" }
        val nodeId = pieces[0]
        val host = pieces[1]
        val port = pieces[2].toInt()
        return NodeEndpoint(nodeId, host, port)
    }


    private fun buildOptions(): Options {
        val options = Options()
        // 模式
        options.addOption(
            Option.builder("m")
                .hasArg()
                .argName("mode")
                .desc("start mode, available: standalone, standby, group-member. default is standalone")
                .build()
        )
        // 节点 ID
        options.addOption(
            Option.builder("i")
                .longOpt("id")
                .hasArg()
                .argName("node-id")
                .required()
                .desc(
                    "node id, required. must be unique in group. " +
                            "if starts with mode group-member, please ensure id in group config"
                )
                .build()
        )
        // 主机名
        options.addOption(
            Option.builder("h")
                .hasArg()
                .argName("host")
                .desc("host, required when starts with standalone or standby mode")
                .build()
        )
        // Raft 端口
        options.addOption(
            Option.builder("p1")
                .longOpt("port-raft-node")
                .hasArg()
                .argName("port")
                .type(Number::class.java)
                .desc("port of raft node, required when starts with standalone or standby mode")
                .build()
        )
        // KV 端口
        options.addOption(
            Option.builder("p2")
                .longOpt("port-service")
                .hasArg()
                .argName("port")
                .type(Number::class.java)
                .required()
                .desc("port of service, required")
                .build()
        )
        // 日志目录
        options.addOption(
            Option.builder("d")
                .hasArg()
                .argName("data-dir")
                .desc("data directory, optional. must be present")
                .build()
        )
        // 集群配置
        options.addOption(
            Option.builder("gc")
                .hasArgs()
                .argName("node-endpoint")
                .desc(
                    "group config, required when starts with group-member mode. " +
                            "format: <node-endpoint> <node-endpoint>..., " +
                            "format of node-endpoint: <node-id>,<host>,<port-raft-node>, " +
                            "eg: A,localhost,8000 B,localhost,8010"
                )
                .build()
        )
        return options
    }
}

fun main(args: Array<String>) {
    val launcher = ServerLauncher()
    launcher.start(args)
}