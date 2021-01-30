package com.qianlei.kv.message

import io.netty.channel.Channel

/**
 *
 * @author qianlei
 */
class CommandRequest<T>(val command: T, private val channel: Channel) {
    fun reply(response: Any) {
        channel.writeAndFlush(response)
    }

    fun addCloseListener(runnable: Runnable) {
        channel.closeFuture().addListener { runnable.run() }
    }
}