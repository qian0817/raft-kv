package com.qianlei.rpc.nio

import com.qianlei.node.NodeId
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author qianlei
 */
class InboundChannelGroup {
    private val logger = KotlinLogging.logger { }
    private val channels = CopyOnWriteArrayList<NioChannel>()

    fun add(remoteId: NodeId, channel: NioChannel) {
        logger.debug { "channel INBOUND-${remoteId} connected" }
        channel.nettyChannel.closeFuture().addListener {
            logger.debug { "channel INBOUND-${remoteId} disconnected" }
            remove(channel)
        }
    }

    private fun remove(channel: NioChannel) {
        channels.remove(channel)
    }

    fun closeAll() {
        logger.debug { "close all inbound channels" }
        channels.forEach(NioChannel::close)
    }

}