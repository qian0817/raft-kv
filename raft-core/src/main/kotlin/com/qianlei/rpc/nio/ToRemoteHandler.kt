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
class ToRemoteHandler(
    eventBus: EventBus,
    remoteId: NodeId?,
    private val selfNodeId: NodeId
) : AbstractHandler(eventBus) {
    init {
        super.remoteId = remoteId
    }

    private val logger = KotlinLogging.logger { }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.write(selfNodeId)
        channel = NioChannel(ctx.channel())
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        logger.debug("receive {} from {}", msg, remoteId)
        super.channelRead(ctx, msg)
    }
}