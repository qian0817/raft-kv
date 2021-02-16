package com.qianlei.kv.server.message

import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponse

data class CommandRequest<T>(
    val command: T,
    val channel: Channel
) {
    fun reply(response: HttpResponse) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }
}