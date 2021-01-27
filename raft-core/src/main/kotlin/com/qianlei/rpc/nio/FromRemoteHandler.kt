package com.qianlei.rpc.nio

import com.google.common.eventbus.EventBus
import com.qianlei.node.NodeId
import io.netty.channel.ChannelHandlerContext
import mu.KotlinLogging

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
class FromRemoteHandler(
    eventBus: EventBus,
    private val channelGroup: InboundChannelGroup
) : AbstractHandler(eventBus) {
    private val logger = KotlinLogging.logger { }
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is NodeId) {
            remoteId = msg
            val nioChannel = NioChannel(ctx.channel())
            channel = nioChannel
            channelGroup.add(msg, nioChannel)
        } else {
            super.channelRead(ctx, msg)
        }
    }
}