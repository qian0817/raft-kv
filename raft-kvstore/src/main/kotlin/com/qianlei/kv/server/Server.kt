package com.qianlei.kv.server

import com.qianlei.node.Node
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import mu.KotlinLogging

/**
 *
 * @author qianlei
 */
class Server(private val node: Node, private val port: Int) {
    private val logger = KotlinLogging.logger { }
    private val service = Service(node)
    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup(4)

    fun start() {
        node.start()
        val serverBootStrap = ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(ServiceDecoder())
                        .addLast(ServiceEncoder())
                        .addLast(ServiceHandler(service))
                }
            })
        logger.info { "server started at port $port" }
        serverBootStrap.bind(port)
    }

    fun stop() {
        logger.info("stopping server")
        this.node.stop()
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }
}