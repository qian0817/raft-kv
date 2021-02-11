package com.qianlei.kv.server

import com.qianlei.kv.message.CommandRequest
import com.qianlei.kv.message.GetCommand
import com.qianlei.kv.message.SetCommand
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 *
 * @author qianlei
 */
class ServiceHandler(private val service: Service) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is GetCommand -> service.get(CommandRequest(msg, ctx.channel()))
            is SetCommand -> service.set(CommandRequest(msg, ctx.channel()))
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }
}