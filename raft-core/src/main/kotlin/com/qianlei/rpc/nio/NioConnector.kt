package com.qianlei.rpc.nio

import com.google.common.eventbus.EventBus
import com.qianlei.node.NodeEndpoint
import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel
import com.qianlei.rpc.Connector
import com.qianlei.rpc.SocketChannelUtil
import com.qianlei.rpc.message.*
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import mu.KotlinLogging
import java.util.concurrent.Executors

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class NioConnector(
    private val workerNioEventLoopGroup: NioEventLoopGroup,
    private val workedGroupShared: Boolean,
    selfNodeId: NodeId,
    private val eventBus: EventBus,
    private val port: Int
) : Connector {
    private val logger = KotlinLogging.logger { }
    private val bossNioEventLoopGroup = NioEventLoopGroup()
    private val inboundChannelGroup = InboundChannelGroup()
    private val outboundChannelGroup = OutboundChannelGroup(workerNioEventLoopGroup, eventBus, selfNodeId)
    private val executorService = Executors.newCachedThreadPool()

    override fun initialize() {
        val serverBootstrap = ServerBootstrap().group(bossNioEventLoopGroup, workerNioEventLoopGroup)
            .channel(SocketChannelUtil.findSupportServerChannel())
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(Encoder())
                        .addLast(Decoder())
                        .addLast(FromRemoteHandler(eventBus, inboundChannelGroup))
                }
            })
        logger.info { "node listen on port $port" }
        serverBootstrap.bind(port).sync()
    }


    override fun sendRequestVote(rpc: RequestVoteRpc, destinationEndpoints: Collection<NodeEndpoint>) {
        destinationEndpoints.forEach { endpoint ->
            executorService.submit {
                try {
                    getChannel(endpoint).writeRequestVoteRpc(rpc)
                } catch (e: Exception) {
                    logger.warn { "failed to send RequestVoteRpc to ${endpoint.id}" }
                }
            }
        }
    }

    private fun getChannel(endpoint: NodeEndpoint): Channel {
        return outboundChannelGroup.getOrConnect(endpoint.id, endpoint.address)
    }

    override fun replyRequestVote(result: RequestVoteResult, destinationEndpoint: NodeEndpoint) {
        executorService.submit {
            try {
                getChannel(destinationEndpoint).writeRequestVoteResult(result)
            } catch (e: Exception) {
                logger.warn { "failed to send RequestVoteResult to ${destinationEndpoint.id}" }
            }
        }
    }

    override fun sendAppendEntries(rpc: AppendEntriesRpc, destinationEndpoint: NodeEndpoint) {
        executorService.submit {
            try {
                getChannel(destinationEndpoint).writeAppendEntriesRpc(rpc)
            } catch (e: Exception) {
                logger.warn { "failed to send AppendEntriesRpc to ${destinationEndpoint.id}" }
            }
        }
    }

    override fun replyAppendEntries(result: AppendEntriesResult, destinationEndpoint: NodeEndpoint) {
        executorService.submit {
            try {
                getChannel(destinationEndpoint).writeAppendEntriesResult(result)
            } catch (e: Exception) {
                logger.warn { "failed to send AppendEntriesResult to ${destinationEndpoint.id}" }
            }
        }
    }

    override fun replyInstallSnapshot(result: InstallSnapshotResult, destinationEndpoint: NodeEndpoint) {
        executorService.submit {
            try {
                getChannel(destinationEndpoint).writeInstallSnapshotResult(result)
            } catch (e: Exception) {
                logger.warn { "failed to send InstallSnapshotResult to ${destinationEndpoint.id}" }
            }
        }
    }

    override fun sendInstallSnapshot(rpc: InstallSnapshotRpc, destinationEndpoint: NodeEndpoint) {
        executorService.submit {
            try {
                getChannel(destinationEndpoint).writeInstallSnapshotRpc(rpc)
            } catch (e: Exception) {
                logger.warn { "failed to send InstallSnapshotRpc to ${destinationEndpoint.id}" }
            }
        }
    }

    override fun resetChannels() {
        inboundChannelGroup.closeAll()
    }

    override fun close() {
        logger.info { "close connector" }
        inboundChannelGroup.closeAll()
        outboundChannelGroup.closeAll()
        bossNioEventLoopGroup.shutdownGracefully()
        if (!workedGroupShared) {
            workerNioEventLoopGroup.shutdownGracefully()
        }
    }
}