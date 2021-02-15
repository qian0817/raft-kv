package com.qianlei.rpc.nio

import com.google.common.eventbus.EventBus
import com.qianlei.node.NodeEndpoint
import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel
import com.qianlei.rpc.Connector
import com.qianlei.rpc.message.*
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import mu.KotlinLogging

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

    override fun initialize() {
        val serverBootstrap = ServerBootstrap().group(bossNioEventLoopGroup, workerNioEventLoopGroup)
            .channel(NioServerSocketChannel::class.java)
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
        destinationEndpoints.forEach {
            try {
                getChannel(it).writeRequestVoteRpc(rpc)
            } catch (e: Exception) {
                logger.warn { "failed to send RequestVoteRpc to ${it.id}" }
            }
        }
    }

    private fun getChannel(endpoint: NodeEndpoint): Channel {
        return outboundChannelGroup.getOrConnect(endpoint.id, endpoint.address)
    }

    override fun replyRequestVote(result: RequestVoteResult, destinationEndpoint: NodeEndpoint) {
        try {
            getChannel(destinationEndpoint).writeRequestVoteResult(result)
        } catch (e: Exception) {
            logger.warn { "failed to send RequestVoteRpc to ${destinationEndpoint.id}" }
        }
    }

    override fun sendAppendEntries(rpc: AppendEntriesRpc, destinationEndpoint: NodeEndpoint) {
        try {
            getChannel(destinationEndpoint).writeAppendEntriesRpc(rpc)
        } catch (e: Exception) {
            logger.warn { "failed to send RequestVoteRpc to ${destinationEndpoint.id}" }
        }
    }

    override fun replyAppendEntries(result: AppendEntriesResult, destinationEndpoint: NodeEndpoint) {
        try {
            getChannel(destinationEndpoint).writeAppendEntriesResult(result)
        } catch (e: Exception) {
            logger.warn { "failed to send RequestVoteRpc to ${destinationEndpoint.id}" }
        }
    }

    override fun replyInstallSnapshot(result: InstallSnapshotResult, destinationEndpoint: NodeEndpoint) {
        getChannel(destinationEndpoint).writeInstallSnapshotResult(result)
    }

    override fun sendInstallSnapshot(rpc: InstallSnapshotRpc, destinationEndpoint: NodeEndpoint) {
        getChannel(destinationEndpoint).writeInstallSnapshotRpc(rpc)
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