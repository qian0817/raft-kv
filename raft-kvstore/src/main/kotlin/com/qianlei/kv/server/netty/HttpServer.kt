package com.qianlei.kv.server.netty

import com.qianlei.kv.server.KVService
import com.qianlei.kv.server.config.ServerConfig
import com.qianlei.rpc.SocketChannelUtil
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import java.net.InetSocketAddress


/**
 *
 * @author qianlei
 */
class HttpServer(private val service: KVService, private val serverConfig: ServerConfig) {
    fun start() {
        val bootstrap = ServerBootstrap()
            .group(NioEventLoopGroup())
            .channel(SocketChannelUtil.findSupportServerChannel())
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(HttpServerCodec())
                        .addLast(HttpObjectAggregator(512 * 1024))
                        .addLast(HttpRequestHandler(service))
                }
            })
        val f = bootstrap.bind(InetSocketAddress(serverConfig.port)).sync()
        f.channel().closeFuture().sync()
    }
}