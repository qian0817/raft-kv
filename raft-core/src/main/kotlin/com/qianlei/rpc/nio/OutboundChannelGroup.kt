package com.qianlei.rpc.nio

import com.google.common.eventbus.EventBus
import com.qianlei.node.NodeId
import com.qianlei.rpc.Address
import com.qianlei.rpc.Channel
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import mu.KotlinLogging
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.FutureTask


/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class OutboundChannelGroup(
    private val workerNioEventLoopGroup: NioEventLoopGroup,
    private val eventBus: EventBus,
    private val selfNodeId: NodeId
) {
    private val logger = KotlinLogging.logger { }
    private val channelMap = ConcurrentHashMap<NodeId, Future<NioChannel>>()

    private fun connect(nodeId: NodeId, address: Address): NioChannel {
        val bootstrap = Bootstrap()
            .group(workerNioEventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(Decoder())
                        .addLast(Encoder())
                        .addLast(ToRemoteHandler(eventBus, nodeId, selfNodeId))
                }
            })
        val future = bootstrap.connect(address.host, address.port).sync()
        if (!future.isSuccess) {
            throw IOException("failed to connect", future.cause())
        }
        logger.debug { "channel OUTBOUND-$nodeId connected" }
        val nettyChannel = future.channel()
        nettyChannel.closeFuture().addListener {
            logger.debug { "channel OUTBOUND- $nodeId disconnected" }
            channelMap.remove(nodeId)
        }
        return NioChannel(nettyChannel)
    }

    fun getOrConnect(nodeId: NodeId, address: Address): Channel {
        var future = channelMap[nodeId]
        if (future == null) {
            val newFuture = FutureTask { connect(nodeId, address) }
            future = channelMap.putIfAbsent(nodeId, newFuture)
            if (future == null) {
                newFuture.run()
                return newFuture.get()
            }
        }
        try {
            return future.get()
        } catch (e: Exception) {
            channelMap.remove(nodeId)
            throw IOException("failed to get channel to node $nodeId", e)
        }
    }

    fun closeAll() {
        logger.debug("close all outbound channels")
        channelMap.forEach { (_, nioChannelFuture) ->
            try {
                nioChannelFuture.get().close()
            } catch (e: Exception) {
                logger.warn("failed to close", e)
            }
        }
    }
}