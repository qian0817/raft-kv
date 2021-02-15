package com.qianlei.rpc.nio

import com.google.common.eventbus.EventBus
import com.qianlei.node.NodeId
import com.qianlei.rpc.Channel
import com.qianlei.rpc.message.*
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import mu.KotlinLogging

/**
 *
 * @author qianlei
 */
@Suppress("UnstableApiUsage")
abstract class AbstractHandler(private val eventBus: EventBus) : ChannelDuplexHandler() {
    companion object {
        @Volatile
        private var lastAppendEntriesRpc: AppendEntriesRpc? = null

        @Volatile
        private var lastInstallSnapshotRpc: InstallSnapshotRpc? = null
    }

    private val logger = KotlinLogging.logger { }
    protected var channel: Channel? = null
    protected var remoteId: NodeId? = null

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val remoteId = checkNotNull(remoteId)
        val lastReceived = lastAppendEntriesRpc
        when (msg) {
            is RequestVoteRpc -> eventBus.post(RequestVoteRpcMessage(msg, remoteId, channel))
            is RequestVoteResult -> eventBus.post(msg)
            is AppendEntriesResult -> {
                if (lastReceived == null) {
                    logger.warn { "no last append entries rpc" }
                } else {
                    eventBus.post(eventBus.post(AppendEntriesResultMessage(msg, remoteId, lastReceived)))
                }
            }
            is AppendEntriesRpc -> {
                eventBus.post(AppendEntriesRpcMessage(msg, remoteId, channel))
            }
            is InstallSnapshotRpc -> eventBus.post(InstallSnapshotRpcMessage(msg, remoteId, channel))
            is InstallSnapshotResult -> {
                val rpc = lastInstallSnapshotRpc
                if (rpc == null) {
                    logger.warn { "lastInstallSnapshotRpc is null" }
                    return
                }
                eventBus.post(InstallSnapshotResultMessage(msg, remoteId, rpc))
                lastInstallSnapshotRpc = null
            }
        }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is AppendEntriesRpc) {
            lastAppendEntriesRpc = msg
        } else if (msg is InstallSnapshotRpc) {
            lastInstallSnapshotRpc = msg
        }
        super.write(ctx, msg, promise)
    }
}