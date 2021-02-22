package com.qianlei.rpc

import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

object SocketChannelUtil {
    /**
     * 查找最适合的 ServerChannel
     */
    fun findSupportServerChannel(): Class<out ServerChannel> {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel::class.java
        }
        if (KQueue.isAvailable()) {
            return KQueueServerSocketChannel::class.java
        }
        return NioServerSocketChannel::class.java
    }
}